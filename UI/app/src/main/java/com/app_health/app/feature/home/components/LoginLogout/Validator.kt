package com.app_health.app.feature.home.components.LoginLogout


object AuthValidator {

    fun validateName(name: String): String? {
        if (name.trim().isEmpty()) {
            return "Không được để trống họ và tên"
        }
        return null
    }

    fun validateEmail(email: String): String? {
        if (email.trim().isEmpty()) {
            return "Không được để trống email"
        }
        if (!email.trim().endsWith("@gmail.com")) {
            return "Email phải có đuôi @gmail.com"
        }
        return null
    }

    fun validateRegisterPassword(password: String): String? {
        if (password.isEmpty()) {
            return "Không được để trống mật khẩu"
        }
        if (password.length < 5) {
            return "Mật khẩu phải có ít nhất 5 ký tự"
        }
        if (!password.any { it.isUpperCase() }) {
            return "Mật khẩu phải chứa ít nhất 1 chữ in hoa"
        }
        if (!password.any { it.isDigit() }) {
            return "Mật khẩu phải chứa ít nhất 1 chữ số"
        }
        val specialCharacters = "-@%\\[\\}+'!/#$^?:;,\\(\"\\)~`.*=&\\{>\\]<_"
        if (!password.any { specialCharacters.contains(it) }) {
            return "Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt"
        }
        return null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? {
        if (confirm.isEmpty()) {
            return "Vui lòng xác nhận mật khẩu"
        }
        if (password != confirm) {
            return "Bạn đã nhập sai mật khẩu xác nhận"
        }
        return null
    }

    fun validateLoginPassword(password: String): String? {
        if (password.isEmpty()) {
            return "Không được để trống mật khẩu"
        }
        return null
    }
}