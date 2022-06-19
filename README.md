# CenterInvestCV

Документация

Для интеграции данной библиотеки в существующий проект необходимо:

1. Добавить библиотеку в зависимости в файле gradle
2. Прописать следующие разрешения в файле AndroidManifest.xml:
```xml
<uses-feature android:name="android.hardware.camera.any" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

3. Создать обработчик запроса разрешения во фрагменте следующим образом:
```kotlin
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    if (requestCode == Camera.REQUEST_CODE_PERMISSIONS) {
        if (camera?.allPermissionsGranted(requireContext()) == true) {
            camera?.startCamera(Camera.FaceAnalyzerType.Detect)
        } else {
            Toast.makeText(
                context,
                getString(R.string.permission_was_not_granted),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
```
И прописать запрос разрешения следующим образом:
```kotlin
ActivityCompat.requestPermissions(
    requireActivity(),
    Camera.REQUIRED_PERMISSIONS,
    Camera.REQUEST_CODE_PERMISSIONS
)
```

4. Создать PreviewView в xml файле, на котором будет использоваться скрытие данных/добавление нового лица, со следующими обязательными параметрами:
```xml
<androidx.camera.view.PreviewView
    android:id="@+id/previewView"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    app:layout_constraintDimensionRatio="3:4" />
```
5. Унаследовать фрагмент от интерфейса Camera.CameraListener и реализовать необходимые методы. Интерфейс Camera.CameraListener включает в себя следующие методы:
```kotlin
fun drawOverlay(faceBounds: List<RectF>): Unit? = null – для отрисовки прямоугольника лица человека на PreviewView, если это необходимо

fun drawFace(faces: List<Bitmap>): Unit? = null – для отрисовки лица на экране добавления лица

fun hideData(hide: Boolean): Unit? = null – для скрытия данных пользователя. Как именно будут скрываться данные зависит от реализации этого метода
```
5. Создать объект Camera и запустить камеру следующим образом:
```kotlin
binding.previewView.doOnLayout {
    camera = Camera(
        requireContext(),
        this,
        faceNetModel,
        lensFacing,
        it,
        binding.previewView.surfaceProvider
    )
    if (camera?.allPermissionsGranted() == true) {
        camera?.startCamera(Camera.FaceAnalyzerType.Detect)
    } else {
        ActivityCompat.requestPermissions(
            requireActivity(),
            Camera.REQUIRED_PERMISSIONS,
            Camera.REQUEST_CODE_PERMISSIONS
        )
    }
    camera?.attachListener(this)

    viewModel.loadAllFaceEntities()
}
```
Пояснение
```kotlin
lensFacing – камера, с которой будет производиться захват изображения (фронтальная - CameraSelector.LENS_FACING_FRONT или задняя - CameraSelector.LENS_FACING_BACK) 
faceNetModel – настройки предобученной модели. Рекомендуемые настройки:
faceNetModel = FaceNetModel(
    context = requireContext(),
    model = TensorflowModels.FACENET,
    useGpu = true,
    useXNNPack = true
)
binding.previewView – PreviewView, которое было добавлено в xml файл ранее
```
Запуск камеры для распознавания лиц происходит с помощью метода `camera?.startCamera(Camera.FaceAnalyzerType.*)`
```kotlin
Camera.FaceAnalyzerType – тип детектора лиц. Включает в себя следующие подтипы:
Camera.FaceAnalyzerType.Detect необходимо использовать для скрытия данных
Camera.FaceAnalyzerType.Recognize – для добавления лица
```
`kotlin viewModel.loadAllFaceEntities()` – подгрузка всех лиц из базы данных. Эта функция запрашивает данные из локальной базы данных с помощью метода репозитория `kotlin roomFaceRepository.loadAllFaceEntities()` данной библиотеки, где roomFaceRepository – объект класса RoomFaceRepository

Так же в репозитории RoomFaceRepository данной библиотеки есть методы для сохранения/удаления лица и редактирования названия лица:
```kotlin
fun addFaceEntity(faceEntity: FaceEntity): Completable – добавление лица
fun deleteFaceEntity(id: Int): Completable – удаление лица
fun editFaceEntity(id: Int, newName: String): Completable – редактирование названия лица
```
Для вызова этих методов необходимо использовать RxJava.

Для сохранения лица в метод addFaceEntity необходимо передать объект FaceEntity, в который нужно передать следующие поля:
```kotlin
name: String – название лица
imageData: FloatArray – преобразованное изображение лица, которое можно получить, вызвав функцию camera.getFaceEmbedding(camera.currentFace)
```
