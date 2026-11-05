package com.app_health.app.feature.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app_health.app.feature.auth.components.AuthTextField
import com.app_health.app.feature.home.components.LoginLogout.AuthValidator
import com.app_health.app.feature.home.components.LoginLogout.DataAuthManager
import com.app_health.app.feature.home.components.LoginLogout.CurrentUserManager
import com.app_health.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUserManager = remember { CurrentUserManager(context) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(AppPrimary.copy(alpha = 0.15f), AppBackground),
        startY = 0f,
        endY = 1200f
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(text = "Bắt đầu ngay!", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Tạo tài khoản để theo dõi dinh dưỡng",
            color = TextSecondary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

        AuthTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = "Họ và tên",
            icon = Icons.Default.Person,
            errorMessage = nameError
        )
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = "Email",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            errorMessage = emailError
        )
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it; passwordError = null; if (confirmPasswordError != null) confirmPasswordError = null },
            label = "Mật khẩu",
            icon = Icons.Default.Lock,
            isPassword = true,
            errorMessage = passwordError
        )
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = null },
            label = "Xác nhận mật khẩu",
            icon = Icons.Default.Lock,
            isPassword = true,
            errorMessage = confirmPasswordError
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                nameError = AuthValidator.validateName(name)
                emailError = AuthValidator.validateEmail(email)
                passwordError = AuthValidator.validateRegisterPassword(password)
                confirmPasswordError = AuthValidator.validateConfirmPassword(password, confirmPassword)

                if (nameError == null && emailError == null && passwordError == null && confirmPasswordError == null) {
                    coroutineScope.launch {
                        val cleanEmail = email.trim()
                        val authManager = DataAuthManager(context, cleanEmail)

                        // Lưu thông tin user vào DataStore riêng biệt dựa theo email
                        authManager.saveUser(name.trim(), cleanEmail, password)

                        // Lưu phiên đăng nhập hiện tại để các màn hình khác nhận diện đúng user
                        currentUserManager.saveCurrentEmail(cleanEmail)

                        onRegisterSuccess()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Đăng Ký", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Đã có tài khoản? ", color = TextSecondary)
            Text(
                text = "Đăng nhập",
                color = AppPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onNavigateToLogin = {},
        onRegisterSuccess = {}
    )
}