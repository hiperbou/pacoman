package gameplay


import scenes.GameScene

var puntuacion_antigua = 0
var puntuacion_max = 0

class GameState() {
    var paused = false
    //var pauseBalls = false
    //var restarting = false

    var nivel = 1
    var volumen = 0
    var muerte = 0
    var puntuacion = 0
    var puntos = 0

    var ividas = 0
    val vidas = mutableListOf<GameScene.vida>()              // Identificadores para las vidas del marcador

    var idpaco:GameScene.paco? = null


    //val arrowCollisions = Collisions<GameScene.disparo_nave>()
    //val ballCollision = Collisions<GameScene.asteroide>()
    //val playerCollision = Collisions<GameScene.nave>()

    fun resetCollisions(){
        //arrowCollisions.reset()
        //ballCollision.reset()
        //playerCollision.reset()
    }
}

var currentGameState = GameState()
