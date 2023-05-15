package resources

import korlibs.audio.sound.Sound
import korlibs.audio.sound.readSound
import korlibs.image.atlas.Atlas
import korlibs.image.atlas.readAtlas
import korlibs.image.font.BitmapFont
import korlibs.image.font.readBitmapFont
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.view.Views
import kotlin.native.concurrent.*

class Resources(private val views: Views) {
    @ThreadLocal
    companion object{
        lateinit var atlas: Atlas

        lateinit var font: BitmapFont
        lateinit var s_come_fantasma: Sound
        lateinit var s_come_fruta: Sound
        lateinit var s_come_punto: Sound
        lateinit var s_come_puntogr: Sound
        lateinit var s_empieza: Sound
        lateinit var s_muerte: Sound
        lateinit var s_inicio: Sound

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

        atlas = resourcesVfs["fpg.atlas.json"].readAtlas()
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

