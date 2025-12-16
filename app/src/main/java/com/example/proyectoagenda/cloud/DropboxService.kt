package com.example.proyectoagenda.cloud

import android.content.Context
import android.util.Log
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class DropboxService(private val context: Context) {

    // Tus constantes solicitadas
    private val PREFS_NAME = "dropbox_prefs"
    private val ACCESS_TOKEN_KEY = "access_token"

    // Esta es solo para uso interno del servicio si necesitara regenerar algo,
    // pero el Auth se hace en la UI.
    private val APP_KEY = "17wsfmwhq502yjt"

    private val sharedPrefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val dbxConfig =
        DbxRequestConfig.newBuilder("AgendaApp").build()

    private var dbxClient: DbxClientV2? = null

    init {
        // Al iniciar, intentamos recuperar el token guardado
        val savedToken = sharedPrefs.getString(ACCESS_TOKEN_KEY, null)
        if (savedToken != null) {
            dbxClient = DbxClientV2(dbxConfig, savedToken)
        }
    }

    // --- Gestión de Cliente y Token ---

    fun isClientInitialized(): Boolean {
        return dbxClient != null
    }

    /**
     * Llamado desde la UI cuando Auth.getOAuth2Token() retorna éxito
     */
    fun initClient(token: String) {
        // Guardamos en Prefs
        sharedPrefs.edit().putString(ACCESS_TOKEN_KEY, token).apply()
        // Inicializamos cliente
        dbxClient = DbxClientV2(dbxConfig, token)
        Log.d("DropboxService", "Cliente inicializado con nuevo token.")
    }

    fun logout() {
        sharedPrefs.edit().remove(ACCESS_TOKEN_KEY).apply()
        dbxClient = null
    }

    // --- Operaciones de Archivos (Upload / Download) ---

    suspend fun uploadFile(jsonContent: String, remotePath: String): Boolean = withContext(Dispatchers.IO) {
        if (dbxClient == null) return@withContext false

        try {
            val inputStream = ByteArrayInputStream(jsonContent.toByteArray(Charsets.UTF_8))
            dbxClient!!.files().uploadBuilder(remotePath)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(inputStream)
            return@withContext true
        } catch (e: Exception) {
            Log.e("DropboxService", "Error subiendo: ${e.message}")
            return@withContext false
        }
    }

    suspend fun downloadFile(remotePath: String): String? = withContext(Dispatchers.IO) {
        if (dbxClient == null) return@withContext null

        try {
            val outputStream = ByteArrayOutputStream()
            dbxClient!!.files().download(remotePath).download(outputStream)
            return@withContext outputStream.toString(Charsets.UTF_8.name())
        } catch (e: Exception) {
            Log.e("DropboxService", "Error bajando: ${e.message}")
            return@withContext null
        }
    }
}