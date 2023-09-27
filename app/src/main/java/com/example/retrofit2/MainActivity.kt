package com.example.retrofit2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.retrofit2.ui.theme.Retrofit2Theme
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

const val BASE_URL = "http://192.168.8.14/"
private val compositeDisposable = CompositeDisposable()

//Main activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Retrofit2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val dataTextState = remember {
                        mutableStateOf("")
                    }
                    val apiService = createApiService()
                    getSensorData(dataTextState, apiService)
                    Column {
                        Button(
                            onClick = { toggleLight(apiService) }
                        ) {
                            Text(text = "Toggle Light")
                        }

                        SensorDataText(dataText = dataTextState.value)
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Clear the disposables when the activity is destroyed
        compositeDisposable.clear()
    }
}

private fun createApiService(): ApiInterface {
    return Retrofit.Builder()
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL) // URL of the site
        .build()
        .create(ApiInterface::class.java) // :: references to ApiInterface
}

private fun toggleLight(apiService: ApiInterface) {
    val call = apiService.switchLED()

    val disposable = call
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            // Success
            Log.i("OnLED", "Request Succeed: ")
        }, {
            // Error
            Log.i("OnLED", "Request failed: " + it.message)
        })

    compositeDisposable.add(disposable)
}

private fun getSensorData(dataText: MutableState<String>, apiService: ApiInterface) {
    val call = apiService.getSensorData()

    val disposable = call
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            // Success
                response -> onResponse(dataText, response)
        }, {
            // Error
            Log.i("OnLED", "Request failed: ")
        })

    compositeDisposable.add(disposable)
}

private fun onResponse(dataText: MutableState<String>, response: Response<List<SensorData>>) {
    val myStringBuilder = StringBuilder()

    for (myData in response.body()!!) {
        myStringBuilder.append(myData.ph)
        myStringBuilder.append("\n")
    }
    dataText.value = myStringBuilder.toString()
}

@Composable
fun SensorDataText(dataText: String, modifier: Modifier = Modifier) {
    Text(
        text = dataText,
        modifier = modifier
    )
}
