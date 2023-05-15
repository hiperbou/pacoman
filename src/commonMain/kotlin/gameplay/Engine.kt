package gameplay


import extensions.toBool
import input.getButtonPressed
import korlibs.audio.sound.*
import korlibs.image.bitmap.*
import korlibs.image.color.RGBA
import korlibs.io.async.*
import korlibs.korge.component.onAttachDetach
import korlibs.korge.component.registerStageComponent
import korlibs.korge.input.keys
import korlibs.korge.render.RenderContext
import korlibs.korge.render.TexturedVertexArray
import korlibs.korge.scene.Scene
import korlibs.korge.view.*
import korlibs.korge.view.collision.CollisionKind
import korlibs.korge.view.collision.findCollision
import korlibs.math.geom.*
import korlibs.memory.setBits
import korlibs.memory.unsetBits
import korlibs.time.Frequency
import kotlinx.coroutines.*
import resources.Resources
import kotlin.random.Random
import kotlin.reflect.*

val PI = kotlin.math.PI

fun rand(from:Double, to:Double) = Random.Default.nextDouble(from, to + 1)
fun rand(from:Int, to:Int) = Random.Default.nextInt(from, to + 1)

//fun rand(from:Int, to:Int) = Random.Default.nextInt(from, to)

fun get_distx(angle:Double, dist:Number) = dist.toDouble() * cos(Angle.Companion.fromRadians(-angle))
fun get_disty(angle:Double, dist:Number) = dist.toDouble() * sin(Angle.Companion.fromRadians(-angle))



/*fun fget_dist(const int &x0, const int &y0, const int &x1,const int &y1)
{
    static int dx;
    static int dy;
    static int min;
    dx = abs(x1-x0);
    dy = abs(y1-y0);
    min = (dx<dy) ? dx : dy;

    return (dx+dy-(min >> 1)-(min >> 2)+(min>>3)+(min>> 4));
}*/

private val imageCache = mutableMapOf<Int, BmpSlice>()
fun getImage(graph:Int): BmpSlice {
    return imageCache.getOrPut(graph) {
        return if(graph==0) Process.emptyImage.slice() else
        Resources.atlas["${graph.toString().padStart(3, '0')}.png"]//.texture
    }
}

private lateinit var currentScene:Scene

abstract class SceneBase:Scene()
{
    private val frameReady = Signal<Unit>()
    private var frameListenerInitialized = false

    suspend fun Process.frame() {
        if(!frameListenerInitialized) {
            frameListenerInitialized = true
            addFixedUpdater(Frequency(24.0)) {
                frameReady.invoke()
            }
        }

        frameReady.waitOneBase()
        while(speed == 0f) {
            frameReady.waitOneBase()
        }
    }

    suspend fun Process.frame(n:Int) {
        (0..n/100).forEach { frame() }
    }

    init {
        currentScene = this
    }

    inline fun <reified T:View>Container.get_id():List<T> {
        return Process.list.filter{ it is T } as List<T>
    }

    fun Container.signalKill(predicate:(View)->Boolean) {
        Process.list.filter(predicate).forEach { it.removeFromParent() }
    }

    inline fun <reified T:View> Container.collision():T? {
        val filter: (View) -> Boolean = { it is T }
        val root: Container = this.root as Container
        val kind: CollisionKind = CollisionKind.GLOBAL_RECT

        val c = root.findCollision(this, kind, filter)
        return  if (c!=null) c as T else null
    }



    fun Container.signalFreeze(predicate:(View)->Boolean) {
        Process.list.filter(predicate).forEach { it.freeze() }
    }

    fun Container.signalWakeup(predicate:(View)->Boolean) {
        Process.list.filter(predicate).forEach { it.wakeup() }
    }



    fun Container.letMeAlone(self:Process) {
        signalKill { it != self }
    }
}

class ImageData {
    val vertices = TexturedVertexArray(4, TexturedVertexArray.QUAD_INDICES)
    var baseBitmap: BitmapCoords = Bitmaps.white
    var anchor: Anchor = Anchor.TOP_LEFT
    val sLeft: Float get() = -anchorDispX
    val sTop: Float get() = -anchorDispY
    val sRight: Float get() = sLeft + bwidth
    val sBottom: Float get() = sTop + bheight
    val bwidth: Float get() = baseBitmap.width.toFloat()
    val bheight: Float get() = baseBitmap.height.toFloat()
    val frameOffsetX: Float get() = baseBitmap.frameOffsetX.toFloat()
    val frameOffsetY: Float get() = baseBitmap.frameOffsetY.toFloat()
    val frameWidth: Float get() = baseBitmap.frameWidth.toFloat()
    val frameHeight: Float get() = baseBitmap.frameHeight.toFloat()
    val anchorDispXNoOffset: Float get() = (anchor.sx * frameWidth)
    val anchorDispYNoOffset: Float get() = (anchor.sy * frameHeight)
    val anchorDispX: Float get() = (anchorDispXNoOffset - frameOffsetX)
    val anchorDispY: Float get() = (anchorDispYNoOffset - frameOffsetY)
    var smoothing: Boolean = true
    var renderBlendMode: BlendMode = BlendMode.NORMAL

    fun drawVertices(ctx: RenderContext) {
        ctx.useBatcher { batch ->
            //batch.texture1212
            //batch.setTemporalUniforms(_programUniforms) {
            batch.drawVertices(
                vertices, ctx.getTex(baseBitmap).base, smoothing, renderBlendMode,
            )
            //}
        }
    }

    fun computeVertices(globalMatrix: Matrix, renderColorMul: RGBA) {
        vertices.quad(0, sLeft, sTop, bwidth, bheight, globalMatrix, baseBitmap, renderColorMul)
    }

}

//abstract class Process(parent: Container) : Image(emptyImage) {
abstract class Process(parent: Container) : Container(), Anchorable {
    companion object {
        val emptyImage = Bitmap32(1,1)

        val list = mutableListOf<Process>()
    }

    open val pname:String
        get() = this::class.simpleName ?: "process"

    private val imageData = ImageData()
    override var anchor: Anchor by imageData::anchor
    var bitmap: BitmapCoords by imageData::baseBitmap
    var smoothing: Boolean by imageData::smoothing

    private var _graph = 0
    var graph:Int
        get() =  _graph
        set(value) {
            _graph = value
            bitmap = getImage(value)
        }

    private var _angle = 0.0
    var angle:Double
        get() =  _angle
        set(value) {
            _angle = value
            rotation = -value.radians
        }

    override fun renderInternal(ctx: RenderContext) {
        imageData.computeVertices(globalMatrix, renderColorMul)
        imageData.drawVertices(ctx)
        super.renderInternal(ctx)
    }

    private var _flags = 0
    var flags:Int
        get() = _flags
        set(value){
            _flags = value
            when(value){
                0 -> { scale(1,1) ; alpha=1.0f }
                1 -> { scale(-1,1); alpha=1.0f }
                2 -> { scale(1,-1); alpha=1.0f }
                3 -> { scale(-1,-1); alpha=1.0f }
                4 -> { scale(1,1); alpha=0.5f }
                5 -> { scale(-1,1); alpha=0.5f }
                6 -> { scale(1,-1); alpha=0.5f }
                7 -> { scale(-1,-1); alpha=0.5f }
            }
        }

    init {
        lateinit var job: Job

        onAttachDetach(
            onAttach = {
                job = launchAsap {
                    //main()
                    //removeFromParent()
                    list.add(this@Process)
                    var action = ::main
                    while (true) {
                        try {
                            action()
                            removeFromParent()
                            break
                        } catch (e: ChangeActionException) {
                            action = e.action
                        }
                    }
                }
            },
            onDetach = {
                job.cancel()
                list.remove(this@Process)
            }
        )

        parent.addChild(this)
        anchor(0.5, 0.5)
        smoothing = false
    }

    abstract suspend fun main()

    inline fun loop(block:()->Unit) {
        while(true) {
            block()
        }
    }

    fun kill() {
        removeFromParent()
    }

    fun freeze() {
        speed = 0.0f
    }

    fun wakeup() {
        speed = 1.0f
    }

    class ChangeActionException(val action: KSuspendFunction0<Unit>) : Exception()

    private var key = 0
    private var keyListener = false


    fun key(k:Int):Boolean {
        if (!keyListener) {
            keyListener = true
            keys {
                down { key = key.setBits(getButtonPressed(it)) }
                up { key = key.unsetBits(getButtonPressed(it)) }
            }
        }
        return (key and k).toBool()
    }

    val scan_code:Int
        get() = key

    fun launch(callback: suspend () -> Unit) = currentScene.launch(callback)
    fun launchImmediately(callback: suspend () -> Unit) = currentScene.launchImmediately(callback)
    fun launchAsap(callback: suspend () -> Unit) = currentScene.launchAsap(callback)

    fun <T>async(callback: suspend () -> T) = currentScene.async(callback)
    fun <T>asyncImmediately(callback: suspend () -> T) = currentScene.asyncImmediately(callback)
    fun <T>asyncAsap(callback: suspend () -> T) = currentScene.asyncAsap(callback)
}


fun Views.registerProcessSystem() {
    registerStageComponent(stage)
}



fun Scene.loop(block:suspend ()->Unit){
    launchImmediately {
        while(true) {
            block()
        }
    }
}


private val mute = false
fun Container.sound(nativeSound: Sound, a:Int, b:Int): SoundChannel {
    //if(mute) return
    return nativeSound.playNoCancel(1.playbackTimes)
}

fun Container.stop_sound(channel:SoundChannel){
    channel.stop()
}
