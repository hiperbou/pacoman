import gameplay.registerProcessSystem
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.math.geom.Size
import scenes.GameScene
import scenes.LoadingScene


suspend fun main() = Korge(
	title = "Pacoman",
	windowSize = Size(640, 480),
	targetFps = 24.0,
	backgroundColor = Colors.BLACK,
) {
	views.registerProcessSystem()
	injector
		.mapPrototype { LoadingScene(/*get()*/) }
		.mapPrototype { GameScene(/*get()*/) }
	sceneContainer().changeTo<GameScene>()
}
