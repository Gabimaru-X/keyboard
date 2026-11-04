package gnosis.keyboard.revamped.engine

import android.content.Context
import android.util.Log

/**
 * Revamped Delight Native Bridge
 *
 * Orchestrates and loads Gboard's consolidated native runtime:
 * - libintegrated_shared_object.so (35.2 MB Core Monolith: Delight5, Mozc, HMM, SODA Speech, ScaNN)
 * - All 14 JNI trampolines & media/graphics acceleration libraries
 */
object RevampedDelightBridge {

    private const val TAG = "RevampedDelightBridge"
    private val loadedLibraries = mutableSetOf<String>()
    private var isCoreInitialized = false

    val ALL_NATIVE_MODULES = listOf(
        "surface_util_jni",
        "androidx.graphics.path",
        "graphics-core",
        "jni_signalinitializer",
        "native_crash_handler_jni",
        "image_processing_util_jni",
        "filterframework_jni",
        "ogg_opus_encoder",
        "scam_partition",
        "scam_partitioner",
        "scam_scorer",
        "expressive_concepts_model_less_predictor_jni_native",
        "grammar-checker_jni",
        "gboard_pipeline_jni",
        "integrated_shared_object"
    )

    fun loadAllNativeModules(context: Context? = null): Set<String> {
        for (lib in ALL_NATIVE_MODULES) {
            loadLibrarySafely(lib)
        }
        isCoreInitialized = isLibraryLoaded("integrated_shared_object")
        return loadedLibraries.toSet()
    }

    fun loadLibrarySafely(libName: String): Boolean {
        if (loadedLibraries.contains(libName)) return true
        return try {
            System.loadLibrary(libName)
            loadedLibraries.add(libName)
            Log.d(TAG, "Successfully initialized native module: $libName")
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Native module $libName initialization warning: ${t.message}")
            false
        }
    }

    fun isLibraryLoaded(libName: String): Boolean = loadedLibraries.contains(libName)

    fun isNativeEngineAvailable(): Boolean = isCoreInitialized
}
