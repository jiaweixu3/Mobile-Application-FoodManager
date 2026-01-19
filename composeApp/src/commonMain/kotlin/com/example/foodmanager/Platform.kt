package com.example.foodmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform