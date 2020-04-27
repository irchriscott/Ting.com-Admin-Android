package com.codepipes.tingadmin.interfaces

interface ImageSelectorListener {
    public fun onMultipleImagesSelected(images: List<String>)
    public fun onSingleImageSelected(image: String)
    public fun onCancel()
}