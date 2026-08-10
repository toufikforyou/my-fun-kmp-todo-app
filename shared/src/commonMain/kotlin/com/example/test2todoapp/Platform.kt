package com.example.test2todoapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform