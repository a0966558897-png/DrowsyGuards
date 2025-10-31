#!/bin/bash

# 修復長行問題的腳本
echo "🔧 修復長行問題..."

# 修復 FatigueAlertManager.kt 中的長行
sed -i '' 's/if (context.checkSelfPermission(android.Manifest.permission.VIBRATE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {/if (context.checkSelfPermission(android.Manifest.permission.VIBRATE) != \n                    android.content.pm.PackageManager.PERMISSION_GRANTED) {/g' user-alert/src/main/java/com/patrick/alert/FatigueAlertManager.kt

# 修復 CameraController.kt 中的長行
sed -i '' 's/private fun onImageCaptured(image: ImageProxy, camera: Camera, imageCapture: ImageCapture) {/private fun onImageCaptured(\n        image: ImageProxy,\n        camera: Camera,\n        imageCapture: ImageCapture\n    ) {/g' camera-input/src/main/java/com/patrick/camera/CameraController.kt

sed -i '' 's/private fun onImageCaptured(image: ImageProxy, camera: Camera, imageCapture: ImageCapture) {/private fun onImageCaptured(\n        image: ImageProxy,\n        camera: Camera,\n        imageCapture: ImageCapture\n    ) {/g' camera-input/src/main/java/com/patrick/camera/CameraController.kt

echo "✅ 長行修復完成" 