package resources

import com.soywiz.korau.sound.NativeSound
import com.soywiz.korau.sound.readSound
import com.soywiz.korge.atlas.Atlas
import com.soywiz.korge.atlas.readAtlas
import com.soywiz.korge.view.Views
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.readBitmapFont

import com.soywiz.korio.file.std.resourcesVfs
import kotlin.native.concurrent.*

class Resources(private val views: Views) {
    @ThreadLocal
    companion object{
        lateinit var atlas: Atlas

        lateinit var font: BitmapFont
        lateinit var s_come_fantasma: NativeSound
        lateinit var s_come_fruta: NativeSound
        lateinit var s_come_punto: NativeSound
        lateinit var s_come_puntogr: NativeSound
        lateinit var s_empieza: NativeSound
        lateinit var s_muerte: NativeSound
        lateinit var s_inicio: NativeSound

        private var loaded = false
        private var loadedGfx = false
        private var loadedMusic = false
    }

    suspend fun loadAll() {
        if(loaded) return
        loaded = true

        loadGfx()
        loadMusic()
    }

    suspend fun loadGfx() {
        if(loadedGfx) return
        loadedGfx = true

        atlas = resourcesVfs["fpg.atlas.json"].readAtlas(views)
        font = resourcesVfs["texts/I-pixel-u.fnt"].readBitmapFont()

        s_come_fantasma = resourcesVfs["comefant.wav"].readSound()
        s_come_fruta = resourcesVfs["comefrut.wav"].readSound()
        s_come_punto = resourcesVfs["comecoco.wav"].readSound()
        s_come_puntogr = resourcesVfs["comegcoc.wav"].readSound()
        s_empieza = resourcesVfs["comienzo.wav"].readSound()
        s_muerte = resourcesVfs["muerto.wav"].readSound()
        s_inicio = resourcesVfs["tambor2.wav"].readSound()


    }

    suspend fun loadMusic() {
        if(loadedMusic) return
        loadedMusic = true

        //music = resourcesVfs["music.mp3"].readNativeSound(true)
    }

    fun setLoaded() {
        loaded = true
    }
}

