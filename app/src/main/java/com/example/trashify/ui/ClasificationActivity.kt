package com.example.trashify.ui

import android.Manifest
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.trashify.data.response.CraftingResponse
import com.example.trashify.data.retrofit.RetrofitInstance
import com.example.trashify.ml.ConvertedModel
import com.example.trashify.ui.ui_util.theme.TrashifyTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Serializable
data class Tutorial( val _id: String, val link: String, val desc: String)

class ClasificationActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrashifyTheme {
                val imageSize = 224
                val classes = arrayOf("Cardboard", "Glass", "Metal", "Organic", "Paper", "Plastic")

                var selectedImage by remember {
                    mutableStateOf<Bitmap?>(null)
                }
                var predictionResult by remember {
                    mutableStateOf<String?>(null)
                }
                var percentages by remember {
                    mutableStateOf<List<Float>>(emptyList())
                }
                var isTutorialDialogVisible by remember { mutableStateOf(false) }
                var tutorialDescription by remember { mutableStateOf("") }


                suspend fun getTutorial(material: String): CraftingResponse {
                    return try {
                        RetrofitInstance.tutorialApiService.getTutorial(material)
                    } catch (e: Exception) {
                        // Handle error, log, atau throw exception jika perlu
                        throw e
                    }
//                    try {
//                        val response = RetrofitInstance.tutorialApiService.getTutorial(material.lowercase())
//                        tutorialDescription = response.desc
//                        isTutorialDialogVisible = true
//                    } catch (e: Exception) {
//                        println(e)
//                        e.printStackTrace()
//                    }
                }

                val contentResolver: ContentResolver = LocalContext.current.contentResolver

                fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
                    return try {
                        val inputStream = contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(inputStream)
                    } catch (e: IOException) {
                        // Handle specific exceptions related to decoding
                        e.printStackTrace()
                        null
                    } catch (e: Exception) {
                        // Handle other exceptions
                        e.printStackTrace()
                        null
                    }
                }

                fun classifyImage(image: Bitmap?) {
                    val model = ConvertedModel.newInstance(applicationContext)

                    // Creates inputs for reference.
                    val inputFeature0 =
                        TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

                    val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
                    byteBuffer.order(ByteOrder.nativeOrder())

                    val intValues = IntArray(imageSize * imageSize)
                    image?.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
                    var pixel = 0

                    // Iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
                    for (i in 0 until imageSize) {
                        for (j in 0 until imageSize) {
                            val value = intValues[pixel++]
                            byteBuffer.putFloat(((value ushr 16 and 0xFF) * (1f / 255f)))
                            byteBuffer.putFloat(((value ushr 8 and 0xFF) * (1f / 255f)))
                            byteBuffer.putFloat((value and 0xFF) * (1f / 255f))
                        }
                    }
                    inputFeature0.loadBuffer(byteBuffer)

                    // Runs model inference and gets result.
                    val outputs = model.process(inputFeature0)
                    val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                    // Update the confidence percentages
                    val confidences = outputFeature0.floatArray
                    percentages = confidences.map { (it * 100) }

                    // Find the index of the class with the biggest confidence.
                    var maxPos = 0
                    var maxConfidence = 0f
                    for (i in confidences.indices) {
                        if (confidences[i] > maxConfidence) {
                            maxConfidence = confidences[i]
                            maxPos = i
                        }
                    }
                    predictionResult = classes[maxPos]
                    model.close()
                }

                fun startPredict(bitmapImage: Bitmap?) {
                    if (bitmapImage == null) {
                        return
                    }
                    val scaledBitmap =
                        Bitmap.createScaledBitmap(bitmapImage, imageSize, imageSize, false)
                    selectedImage = scaledBitmap
                    classifyImage(scaledBitmap)
                }

                val cameraLauncher =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview(),
                        onResult = { bitmapImage ->
                            startPredict(bitmapImage)
                        })

                val photoPickerLauncher =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
                        onResult = { uri ->
                            val bitmapImage = uri?.let { uriToBitmap(contentResolver, it) }
                            startPredict(bitmapImage)
                        })

                val cameraPermissionResultLauncher =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted ->
                            if (isGranted) {
                                cameraLauncher.launch(null)
                            } else {
                                Toast.makeText(
                                    this@ClasificationActivity,
                                    "Camera permission required",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })

                // UI Part
                if (isTutorialDialogVisible) {
                    TutorialDialog(
                        tutorialText = tutorialDescription,
                        onClose = {
                            isTutorialDialogVisible = false
                        }
                    )
                }

                Scaffold(
                    topBar = {
                        TopAppBar(colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ), title = {
                            Text("Scan Result")
                        })
                    },
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box {
                                if (selectedImage != null) {
                                    AsyncImage(
                                        model = selectedImage,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }


                            if (predictionResult != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Classified as : ", style = TextStyle(
                                            fontSize = 20.sp, fontWeight = FontWeight.Bold
                                        ), modifier = Modifier.padding(top = 6.dp)
                                    )
                                    Text(
                                        text = predictionResult!!,
                                        color = Color(0xFFC30000),
                                        style = TextStyle(
                                            fontSize = 27.sp, fontWeight = FontWeight.Bold
                                        ),
                                    )
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        contentPadding = PaddingValues(horizontal = 32.dp)
                                    ) {
                                        items(classes.size) { index ->
                                            Text(
                                                text = "${classes[index]}: ${
                                                    String.format(
                                                        "%.2f", percentages[index]
                                                    )
                                                }%"
                                            )
                                        }
                                    }
                                }
                            }

                            if (predictionResult == null) {
                                Text(text = "No image selected")
                            }

                            Column {
                                Spacer(modifier = Modifier.height(8.dp))

                                if (predictionResult != null){
                                    Button(
                                        onClick = {
                                            GlobalScope.launch(Dispatchers.Main) {
                                                try {
                                                    getTutorial(predictionResult!!)
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        this@ClasificationActivity,
                                                        "NO tutorial",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    e.printStackTrace()
                                                }
                                            }
                                        }, modifier = Modifier.width(350.dp)

                                    ) {
                                        Text(
                                            text = "Crafting Tutorial", style = TextStyle(
                                                fontSize = 21.sp, fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        cameraPermissionResultLauncher.launch(
                                            Manifest.permission.CAMERA
                                        )
                                    }, modifier = Modifier.width(350.dp)

                                ) {
                                    Text(
                                        text = "Take Picture", style = TextStyle(
                                            fontSize = 21.sp, fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }, modifier = Modifier.width(350.dp)
                                ) {
                                    Text(
                                        text = "Launch Gallery", style = TextStyle(
                                            fontSize = 21.sp, fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialDialog(
    tutorialText: String,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text("Tutorial Information")
        },
        text = {
            Text(tutorialText)
        },
        confirmButton = {
            Button(
                onClick = onClose,
            ) {
                Text("Close")
            }
        }
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    TrashifyTheme {
//        Greeting("Android")
//    }
//}