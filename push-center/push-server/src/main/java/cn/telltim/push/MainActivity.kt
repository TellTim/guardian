package cn.telltim.push

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.telltim.push.ui.theme.GuardianTheme
import com.orhanobut.logger.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuardianTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Push Core")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    val context = LocalContext.current
    Button(
        onClick = {
            Logger.t("MainActivity").d("startService")
            android.util.Log.d("MainActivity","startService")
            context.startService(
                Intent("cn.telltim.push.action.CALL").setPackage(context.packageName)
            )
        },//点击事件
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .background(Color.Transparent),//修饰
        border =
        BorderStroke(
            10.dp,
            Brush.radialGradient(listOf(Color.White, Color.Black))
        ),//边框颜色 Brush 中的方法对应
        content = { Text(text = name) },//内容
        contentPadding = PaddingValues(100.dp),//当内容过长的时候才会有效，
        enabled = true,//设置按钮是否可用
        shape = CutCornerShape(30)
    )
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GuardianTheme {
        Greeting("Android")
    }
}