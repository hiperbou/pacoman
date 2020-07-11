package scenes

import com.soywiz.kmem.umod
import com.soywiz.korau.sound.NativeSoundChannel
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.bitmap.extract
import com.soywiz.korim.color.Colors
import extensions.toBool
import gameplay.*
import input.*
import resources.Resources
import resources.Resources.Companion.s_come_fantasma
import resources.Resources.Companion.s_come_fruta
import resources.Resources.Companion.s_come_punto
import resources.Resources.Companion.s_come_puntogr
import resources.Resources.Companion.s_empieza
import resources.Resources.Companion.s_inicio
import resources.Resources.Companion.s_muerte
import kotlin.math.abs

class GameScene() : SceneBase() {

    lateinit var pointsText : Text
    lateinit var levelText : Text
    lateinit var canal_s_inicio:NativeSoundChannel

    val backgroundMap = Bitmap32(640,480)


    // Tabla de dificultad
    val inteligencia = arrayOf(0,10,30,50,65,75,85,90,95,100,100)


    override suspend fun Container.sceneInit() {
        Resources(views).loadAll()
        program()
    }

    fun Container.updatePointsText(){
        pointsText.apply {
            text = "${currentGameState.puntuacion}"
            alignLeftToLeftOf(containerRoot)
        }
    }

    val textList = mutableListOf<Text>()

    val textSize = 32.0
    fun Container.write(x:Int, y:Int, c:Int, str:String, size:Double = textSize)= text(str, size, font = Resources.font).apply {
        position(x,y)
        textList.add(this)
    }
    fun Container.write_int(x:Int, y:Int, c:Int, str:Int)= text(str.toString(), textSize, font = Resources.font).apply {
        position(x,y)
        textList.add(this)
    }

    fun Container.delete_all_text(){
        textList.forEach { it.removeFromParent() }
        textList.clear()
    }

    fun Container.foto(graph:Int, x:Int, y:Int, size_x:Int, z:Int, angle:Double, flags:Int) = Foto(this, graph, x, y, size_x, z, angle, flags)
    inner class Foto(parent:Container, val initialGraph:Int, val xx:Int, val yy:Int, val size_x:Int, val z:Int, val anggle:Double, val fflags:Int): Process(parent){
        override suspend fun main() {
            graph = initialGraph
            position(xx,yy)
            anchor(0.5, 0.5)
            scale(size_x/100.0, size_x/100.0)
            angle = anggle
            smoothing = false
            loop {
                frame()
            }
        }
    }

    inner class program():Process(sceneView) {
        override suspend fun main() {

            val _esc = BUTTON_ESCAPE

            parpadeante(128,56)  // Pone los puntos grandes
            parpadeante(512,56)
            parpadeante(128,364)
            parpadeante(512,364)


            //fichero, gráfico, x, y, angle, size, flags, región
            fun xput(graph:Int, x:Int, y:Int, angle:Double, size:Int, flags:Int, region:Int){
                foto(graph, x, y, size, 0, angle, flags)
            }

            image(backgroundMap)

            while (!key(_esc)) {

                // Ejecuta sonido
                canal_s_inicio = sound(s_inicio, 512, 256);

                currentGameState.nivel = 1;    // Primer nivel

                // Imprime pantalla de fondo
                put(1, 320, 240)
                // Pone textos explicativos
                titulo(write(320,320,1,"Pulsa una tecla para jugar").centerXOnStage())
                write(320,80,1,"Record").centerXOnStage()
                write_int(320,110,1, puntuacion_max).centerXOnStage()
                write(320,430,1,"(c) 96 Daniel Navarro - DIV Game Studio",28.0).centerXOnStage()

                fantasma(320,177,12).apply {
                    flags = flags or 4   // Lo hace transparente
                }       // Crea un fantasma

                fantasma(290,223,16).apply {
                    flags = flags or 4
                }       // Crea otro fantasma

                fantasma(320,223,20).apply{
                    flags = flags or 4
                }
                fantasma(352,223,24).apply {
                    flags = flags or 4
                }

                //fade_on();  // Enciende la pantalla

                // Espera a que se pulse una tecla
                while (scan_code == 0) {
                    frame()
                }

                if (key(_esc)) {      // Sale del programa cuando se pulsa escape
                    //fade_off();
                    println("Gracias por jugar!")
                    //exit
                }

                // Detiene el sonido inicial del menu
                stop_sound(canal_s_inicio);


                // Apaga la pantalla
                //fade_off();
                letMeAlone(this);         // Elimina todos los procesos que hubiera
                delete_all_text()  // Elimina tambi‚n todos los textos

                parpadeante(128,56);    // Crea los puntos gordos
                parpadeante(512,56);
                parpadeante(128,364);
                parpadeante(512,364);

                image(backgroundMap)
                clear_screen();         // Borra la pantalla
                put(1,320,240);       // Y pone el fondo nuevo
                // Pone los dos logos de los laterales
                xput(47,56,260,PI/2,100,0,0);
                xput(47,585,260,-PI/2,100,0,0);
                // Crea los procesos que contabilizan las vidas
                currentGameState.ividas=0;
                currentGameState.vidas.add(vida((currentGameState.ividas*26)+552))
                currentGameState.ividas++;
                currentGameState.vidas.add(vida((currentGameState.ividas*26)+552))
                currentGameState.ividas++;
                // Reinicializa el número de puntos comidos y la puntuación
                currentGameState.puntos=0;
                currentGameState.puntuacion=0;

                // Escribe la puntuación
                pointsText = write_int(100,0,2, currentGameState.puntuacion).alignLeftToLeftOf(sceneView)

                // Llama a un proceso que reinicia el juego
                inicio_juego();

                // Empieza el juego
                // Repite hasta queno tenga vidas o se pulse la tecla escape

                while (currentGameState.ividas>=0 && !key(_esc)){

                    if (currentGameState.puntos==246)  {      // Siguiente nivel
                        currentGameState.nivel++;            // Incremente el nivel (fase)
                        currentGameState.nivel=currentGameState.nivel umod 11; // Solo Niveles de 1 a 10
                        //fade_off();         // Apaga la pantalla
                        letMeAlone(this);     // Borra todos los procesos que hubiera
                        // Crea otros 4 nuevos puntos parpadeantes
                        parpadeante(128,56);
                        parpadeante(512,56);
                        parpadeante(128,364);
                        parpadeante(512,364);
                        // Pone otro fondo de pantalla
                        put(1,320,240);
                        // Pone los dos logos de los laterales
                        xput(47,56,260,PI/2,100,0,0);
                        xput(47,585,260,-PI/2,100,0,0);
                        // Crea los procesos que contabilizan las vidas
                        currentGameState.vidas.clear()
                        (0 until currentGameState.ividas).forEach {
                            currentGameState.vidas.add(vida((it*26)+552))
                        }

                        // Reinicia el número de puntos comidos y llama a un proceso que reinicia el juego
                        currentGameState.puntos=0;
                        inicio_juego();
                    }

                    // Pone las frutas cada vez se comen 100 o 200 puntos (cocos)
                    if (currentGameState.puntos==100 || currentGameState.puntos==200) {
                        fruta();
                        currentGameState.puntos++;
                    }

                    // Da una vida a los 10000 puntos
                    if (currentGameState.puntuacion>=10000 && puntuacion_antigua<10000) {
                        currentGameState.vidas[currentGameState.ividas]=vida(currentGameState.ividas*26+552);
                        ++currentGameState.ividas;
                    }

                    // Da otra vida a los 50000 puntos
                    if (currentGameState.puntuacion>=50000 && puntuacion_antigua<50000) {
                        currentGameState.vidas[currentGameState.ividas]=vida(currentGameState.ividas*26+552);
                        ++currentGameState.ividas;
                    }
                    // Actualiza puntuación antigua, necesaria para dar vidas solo una vez
                        puntuacion_antigua=currentGameState.puntuacion;

                    frame();
                }
                letMeAlone(this); // Elimina todos los procesos que hubiera


                // Pone los dos logos de los laterales
                xput(47,56,260,PI/2,100,0,0);
                xput(47,585,260,-PI/2,100,0,0);
                // Juego nuevo
                write(320,197,1,"Fin del juego").centerXOnStage()
                frame(4000);            // Espera un poco en la última imagen
                //fade_off();             // Apaga la pantalla
                delete_all_text()  // Borra todos los textos que hubiera
                clear_screen();         // Borra la pantalla

                // Nueva maxima puntuación
                if (currentGameState.puntuacion>puntuacion_max) {
                    puntuacion_max=currentGameState.puntuacion;  // Cambia la variable que guarda el valor de maxima puntuación
                // Y graba la maxima puntuación en disco
                //save("datpacomanpacoman.dat",&puntuacion_max,1);
                }
            }
        }
    }

    inner class parpadeante(val xx: Number, val yy: Number) : Process(sceneView) {
        override suspend fun main() {
            position(xx, yy)

            loop {
                graph=10   // Imprime el gráfico
                frame(300) // Espera
                graph=0    // No pone ningún gráfico
                frame(300) // Espera
            }
         }
    }

    inner class vida(val xx: Number) : Process(sceneView) {
        override suspend fun main() {
            position(xx, 14) // Elige la coordenada vertical
            graph=4    // Selecciona gráfico
            loop {
                frame()
            }
        }
    }

    // Tabla con los gráficos de la fruta
    private val g_frutas = arrayListOf(0,48,49,50,50,51,51,52,52,53,53)
    // Tabla con la puntuación de las frutas
    private val valor_frutas = arrayListOf(0,100,300,500,500,700,700,1000,1000,2000,2000)
    // Tabla con los gráficos de la puntuación de la fruta
    private val g_valor_frutas = arrayListOf(0,54,55,56,56,57,57,58,58,59,59)
    inner class fruta() : Process(sceneView) {
        private var tiempofruta=100        // Contador de tiempo

        override suspend fun main() {
            position(320, 270)                  // Asigna las coordenadas y el gráfico
            graph = g_frutas[currentGameState.nivel]
            //z=10;                   // Selecciona el gráfico

            // Repite hasta que tiempofruto sea 0
            while (tiempofruta>0) {
                tiempofruta--;      // Decrementa tiempo de fruta
                // Fruta comida
                if (collision<paco>()!=null) { //TODO
                    sound(s_come_fruta, 512, 256);        // Hace el sonido
                    tiempofruta = -20;                    // Pone otra pausa
                    currentGameState.puntuacion += valor_frutas[currentGameState.nivel];    // Suma puntuación
                    updatePointsText()
                    graph = g_valor_frutas[currentGameState.nivel];        // Pon gráfico con puntos
                }
                frame()
            }

            // Espera el valor que se puso negativo en tiempofruta
            while (tiempofruta<0) {
                tiempofruta++;
                frame();
            }
        }
    }

    inner class titulo(val txt: Text) : Process(sceneView) {
        // Coordenadas verticales de titulo
        private val tabla_grafico = arrayListOf(50,52,53,54,54,53,52,50,48,47,46,46,47,48)
        private var contador0 = 0    // Contador de uso general

        override suspend fun main() {
            position(320, 0) // Elige la coordenada vertical
            graph=47;       // Selecciona gráfico de titulo
            //z=-10;
            loop {
                contador0 = (contador0+1).umod(14)

                // Imprímelo dentro de pantalla
                if (contador0==0) txt.position(txt.x.toInt(),320)

                // Imprímelo fuera de pantalla lo que hace al texto intermitente
                if (contador0==7) txt.position(txt.x.toInt(),640)
                // Mueve el titulo
                y = tabla_grafico[contador0].toDouble()    // Mueve el titulo

                frame()
            }
        }
    }

    inner class inicio_juego() : Process(sceneView) {
        override suspend fun main() {

            // Para cualquier sonido que estuviera sonando
            //stop_sound(all_sound);
            //fade_on();  // Encender la pantalla

            // Inicia el sonido de entrada e imprime los texto necesarios
            sound(s_empieza,512,256);
            val texto1 = write(320,243,1,"!Preparado!").centerOnStage()
            val texto2 = write(300-30,152,1,"Nivel")
            val texto3 = write_int(376,152,1, currentGameState.nivel)

            frame(9000)        // Espera

            // Borra los textos
            texto1.removeFromParent()
            texto2.removeFromParent()
            texto3.removeFromParent()

            // Crea a pacoman y a los fantasma
            fantasma(320,177,12)
            fantasma(290,223,16)
            fantasma(320,223,20)
            fantasma(352,223,24)
            paco()
        }
    }


    fun map_get_pixel(bitmap: Bitmap, x:Number, y:Number):String {
        val rgba = bitmap.getRgba(x.toInt(),y.toInt())
        return "${rgba.r},${rgba.g},${rgba.b}"
    }

    fun map_get_pixel(graph:Int, x:Number, y:Number):String {
        return map_get_pixel(getImage(graph).extract(), x, y)
    }




    fun map_put(dest:Bitmap32, graph:Int, x:Number, y:Number) {
        val src = getImage(graph).extract()
        dest.lock {
            src.copy(0,0, dest, x.toInt() - src.width/2, y.toInt() - src.height/2, src.width, src.height)
            //TODO:
        }

    }


    fun clear_screen() {
        backgroundMap.fill(Colors.BLACK)
    }

    fun get_pixel(x:Number, y:Number):String{
        return map_get_pixel(backgroundMap, x, y)
    }

    fun put(graph:Int, x:Number, y:Number){
        map_put(backgroundMap, graph, x, y)
    }

    inner class ojos(val xx: Number,val yy: Number,val modelo: Int) : Process(sceneView) {

        var imagen = 0  // Numero de gráfico
        override suspend fun main() {
            position(xx, yy) // Elige la coordenada vertical

            loop {
                // Comprueba los lados de la pantalla
                if (x<105) x=105.0
                if (x>554) x=554.0

                // Selecciona la dirección y el gráfico de acuerdo al color del mapa de caminos
                when (map_get_pixel(33,(x-105)/2,(y-1)/2)) {
                    "252,252,84" -> { x-=2; graph=29; }
                    "84,252,84" -> { x+=2; graph=30; }
                    "252,84,84" -> { y+=2; graph=31; }
                    "84,84,252" ->  { y-=2; graph=32; }
                    // Si es el color 11 es que ha llegado a casa, quita los ojos y pone un fantasma
                    "84,252,252" -> { imagen=0;
                        fantasma(x, y, modelo)
                        removeFromParent()
                    }
                    else -> {}
                }
                // Solo muestra los gráficos cada cuatro imagens
                //IF ((imagen AND 3)==0) FRAME; END //Así funciona con Div2
                if ((imagen.umod(3))==0) frame()   //Así funciona con Div GO
                imagen++;
            }
        }
    }

    fun cuenta_dir(x:Number, y:Number):Int {
        return (0..3).count { camino_fantasma(x.toInt(),y.toInt(),it) }   // Devuelve el número de direcciones posibles
    }

    fun color(x:Int, y:Int):String {
        // Comprueba si son los lados de la pantalla
        if ((x<105 || x>534) && (y==225 || y==226))
            return ("252,84,84"); // Devuelve un color de camino

        // Devuelve el color del mapa de durezas
        return map_get_pixel(2,(x-105)/2,(y-1)/2)
    }
    fun color(x:Double, y:Double) = color(x.toInt(), y.toInt())

    //TODO: Se usa para detectar las bolas para comer...
    fun punto(x:Number, y:Number):String? {
        if (get_pixel(x,y) == "0,0,0") // Comprueba que el color no sea incorrecto
            return null          // Devuelve FALSE (= 0 = falso) porque ese color es valido
        else
            return color(x.toInt(),y.toInt()) // Retorna el código de color
    }

    fun camino_paco(x:Number, y:Number):Boolean {
        val numero_color=color(x.toInt(),y.toInt())     // Coge el color del mapa de durezas
        return (numero_color == "84,252,84" ||
                numero_color == "252,84,84" ||
                numero_color == "252,252,84")    // Retorna TRUE si puede avanzar
    }


    // Tabla con el tiempo que dura el efecto de poder comer
    private val tiempo_capsulas = arrayListOf(0,300,240,180,140,120,100,80,60,40,0)
    inner class paco() : Process(sceneView) {
        override suspend fun main() {
            var imagen = 0             // Contador de im genes
            var velocidad_paco=2;   // Velocidad de pacoman
            var incr_x=0;           // Coordenadas relativas
            var incr_y=0;
            var contador0 = listOf<fantasma>() ;          // Contador general
            var comido:fantasma? = null             // Identificador de fantasma
            var grafico_viejo = 0;      // Contenedor temporal de gráficos
            val pasos= arrayListOf(0,1,2,1)    // Tabla de animación
            var paso=0;             // Contador de animación
            var grafico_inicial=3;  // Gráfico inicial
            var puntos_por_comido=0;  // Contador de fantasmas comidos

            currentGameState.idpaco = this;          // Coge el identificador del proceso
            //region=1;           // Hace que el proceso aparezca dentro de la región 1
            graph=3;            // Asigna el gráfico y las coordenadas
            //file=fichero;
            position(320,364)


            val _left = BUTTON_LEFT
            val _right = BUTTON_RIGHT
            val _up = BUTTON_UP
            val _down = BUTTON_DOWN

            val _control = BUTTON_A
            val _space = BUTTON_A


            loop {
                // Lee teclas
                if (key(_space))    // Cambia velocidad si se pulsa la barra espaciadora
                    velocidad_paco = 4
                else
                    velocidad_paco = 2

                // Comprueba si se pulso el cursor de la derecha y puede ir en esa dirección
                if (key(_right) && camino_paco(x + 2, y)) {
                    grafico_inicial = 3;  // Elige el gráfico inicial
                    flags = 0
                    incr_x = 2          // Pone los incrementos vertical y horizontal
                    incr_y = 0
                }

                // Comprueba si se pulso el cursor de la izquierda y puede moverse en esa dirección
                if (key(_left) && camino_paco(x-2,y)) {
                    grafico_inicial=3;
                    flags=1;
                    incr_x=-2;
                    incr_y=0;
                }

                // Comprueba si se pulso el cursor de abajo y puede avanzar
                if (key(_down) && camino_paco(x,y+2)) {
                    grafico_inicial=6;
                    flags=0;
                    incr_y=2;
                    incr_x=0;
                }

                // Comprueba si se pulso el cursor de arriba y puede avanzar
                if (key(_up) && camino_paco(x,y-2)) {
                    grafico_inicial=6;
                    flags=2;
                    incr_y=-2;
                    incr_x=0;
                }

                // Comprueba caminos en la dirección horizontal
                if (camino_paco(x+incr_x,y))
                    x+=incr_x;  // Si se pude mover se mueve
                else {
                    incr_x = 0;   // Detiene el movimiento
                    paso = 1;     // Y reinicia la animación
                }

                // Comprueba caminos en la dirección horizontal
                if (camino_paco(x,y+incr_y))
                    y+=incr_y;  // Si se pude mover se mueve
                else {
                    incr_y = 0;   // Detiene el movimiento
                    paso = 1;     // Y reinicia la animación
                }

                // Selecciona gráficos
                graph = grafico_inicial+pasos[paso];

                // Unicamente muestra la pantalla a la velocidad de paco
                if (imagen >= velocidad_paco) {
                    frame();  // Muestra los gráficos
                    imagen = 0;
                    // Comprueba si ha colisionado con un fantasma
                    comido = collision<fantasma>()
                    if (comido!=null) {

                        // Mira si realmente el fantasma esta cerca
                        if (abs(x - comido!!.x) > 10 || abs(y - comido!!.y) > 10) {
                            comido = null   // Hace como si no se hubieran tocado
                        }

                    }
                    // Actualiza la animación del gráfico
                    if (incr_x != 0 || incr_y != 0) {
                        paso = (paso + 1).umod(4)
                    }
                }
                imagen++;   // Incrementa el contador de imagenes

                // Verifica si pacoman ha salido por los lados
                if (x<=95) x+=450; // Hace que aparezca por el otro lado
                if (x>=546) x-=450;

                // Pacoman ha comido un punto
                if (punto(x,y)=="252,252,84") {
                    currentGameState.puntos++;       // Incrementa el contador de puntos comidos
                    currentGameState.puntuacion += 10; // Incrementa la puntuación
                    updatePointsText()
                    put(9, x, y);   // Borra el punto del mapa de durezas



                    imagen++;       // Incrementa el contador imagenes
                    sound(s_come_punto, 128, 256);    // Hace el sonido
                }

                // Pacoman ha comido un punto grande
                if (punto(x,y)=="84,252,84") {
                    sound(s_come_puntogr, 512, 256);  // Realiza el sonido
                    currentGameState.puntos++;                       // Incrementa el número de puntos comidos
                    currentGameState.puntuacion += 50;                 // Incrementa la puntuación
                    updatePointsText()
                    puntos_por_comido = 0;            // Reinicia la variable que guarda
                    // los puntos que se dan cuando se come a un fantasma
                    put(10, x, y);                  // Quita el punto del mapa de durezas

                    // Pone a todos los fantasmas en estado de ser comidos

                    get_id<fantasma>().forEach {
                        it.estado = tiempo_capsulas[currentGameState.nivel]
                    }
                }

                // Comprueba si ha tocado un fantasma
                if (comido != null) {
                    if (comido!!.estado>0) {      // Pacoman se come al fantasma
                        sound(s_come_fantasma,512,256);         // Hace el sonido

                        // Congela a todos los fantasmas
                        /*contador0=get_id<fantasma>()
                        contador0.forEach {
                            it.freeze()
                        }*/
                        signalFreeze { it is fantasma }

                        grafico_viejo = graph;                    // Guarda el gráfico que tenia
                        graph=0;                                // Quita el gráfico
                        comido!!.graph = 42 + puntos_por_comido      // Cambia el gráfico del fantasma
                        // por el de la puntuación obtenida
                        frame(1500);                            // Espera a que se den 15 imagenes

                        // Vuelve a todos los fantasma al estado en que estaban
                        /*get_id<fantasma>().forEach {
                            it.wakeup()
                        }*/
                        signalWakeup { it is fantasma }
                        graph=grafico_viejo;                    // Recupera el gráfico de paco
                        ojos(comido!!.x,comido!!.y,comido!!.modelo);  // Crea unos ojos de fantasma
                        comido!!.kill()                 // Elimina el fantasma comido

                        currentGameState.puntuacion += when (puntos_por_comido) {        // Da la puntuación según los fantasmas comidos
                            0 -> { 200 }
                            1 -> { 400 }
                            2 -> { 800 }
                            3 -> { 1600 }
                            4 -> { 3200 }
                            else -> { 0 }
                        }
                        updatePointsText()
                        puntos_por_comido++;                    // Incrementa para la proxima vez que coma
                    } else {                    // Fantasma se come a pacoman

                        // Congela a todos los fantasmas
                        get_id<fantasma>().forEach {
                            it.freeze()
                        }
                        frame(1500)        // Espera a que se den 15 imagenes

                        // Elimina los procesos de fantasmas, ojos y frutas
                        signalKill { it is fantasma }
                        signalKill { it is ojos }
                        signalKill { it is fruta }
                        flags=0;                    // Coloca el gráfico sin ningun espejado
                        sound(s_muerte,512,256);    // Realiza el sonido
                        (34..41).forEach  {  // Hace la animación de ser comido
                            graph = it
                            frame(400)
                        }
                        frame(800)                 // Espera 8 imagenes de pantalla
                        if (currentGameState.ividas!=0) {     // Comprueba si le quedan vidas
                            currentGameState.ividas--       // Quita una vida
                            currentGameState.vidas[currentGameState.ividas].removeFromParent()

                            inicio_juego() // Reinicia el juego
                        } else {
                            currentGameState.ividas--;       // Final del juego
                        }
                        removeFromParent()
                        frame()
                    }
                    comido = null               // Borra cualquier colisión detectada que hubiera
                }
            }
        }
    }
    
    fun camino_fantasma(x: Int, y:Int, dir: Number):Boolean {
         // Color 10=Punto grande 11=Casa de fantasma 12=Camino 14=Punto
        // Comprueba la dirección
        var n_color = when (dir) {
             0 -> color(x-2, y)
             1 -> color(x+2, y)
             2 -> color(x, y+2)
             3 -> color(x, y-2)
            else -> "0,0,0"
        }

        // El fantasma no vuelve a entrar en su casa
        if (n_color=="84,252,252" && dir==2 && color(x,y)=="252,84,84")
            n_color="0,0,0";

        // Retorna verdadero (TRUE) si es un camino correcto
        return (n_color=="84,252,252" || n_color=="84,252,84" || n_color=="252,84,84" || n_color=="252,252,84");
    }

    inner class fantasma(val xx: Number, val yy:Number, val modelo: Int) : Process(sceneView) {
        var estado = 0

        var imagen = 0         // Contador de gráficos
        var num_imagenes = 0   // Número de imagenes en que da en pantalla
        var dir = 3          // Dirección 0=izq. 1=der. 2=abajo 3=arriba
        
        
        override suspend fun main() {
            position(xx, yy) // Elige la coordenada vertical
            graph=modelo    // Selecciona gráfico
            loop {
                // Si el fantasma esta en casa entonces da más imagenes
                if (color(x,y)=="84,252,252" || estado>0){
                    num_imagenes=1;
                }else{
                    num_imagenes=2;
                }

                // Si el fantasma esta en un cruce selecciona un camino
                if (cuenta_dir(x.toInt(),y.toInt())>2) {
                    dir = selecciona_dir(x.toInt(),y.toInt(),dir, this); // Devuelve una dirección correcta
                }else{
                    // Si no tiene ningún camino por donde avanza
                    if (!camino_fantasma(x.toInt(),y.toInt(),dir)) {
                        dir = selecciona_dir(x.toInt(),y.toInt(),dir, this);    // Elige otro camino
                    }

                    // Aleatoriamente cambia la dirección si se puede
                    if (rand(0,1000)<2) {
                        dir = selecciona_dir(x.toInt(),y.toInt(),dir xor 1, this)
                    }
                }

                when (dir) {   // Mueve al fantasma
                    0 -> x-=2
                    1 -> x+=2
                    2 -> y+=2
                    3 -> y-=2
                }

                // Comprueba si se sale por los lados de la pantalla
                if (x<=95) { x+=450; }
                if (x>=546) { x-=450; }

                // Comprueba el estado del fantasma
                if (estado==0) {
                    graph = modelo+dir;   // Elige un gráfico de la dirección en estado normal
                }else{
                    // El fantasma puede ser comido y parpadea cambiando gráficos
                    if (estado < 70 && !(estado umod 7).toBool()) {
                        graph = 28;
                    } else {
                        graph = 11;
                    }
                    estado--;           // Decrementa el contador de estado del fantasma
                }

                // Unicamente muestra la imagen a la velocidad del fantasma
                if (imagen>=num_imagenes){
                    frame()
                    imagen=0;
                }
                imagen++;               // Incrementa el contador de imagenes
            }
        }
    }

    fun selecciona_dir(x:Int,y:Int,old_dir:Int, father:fantasma):Int{
        val dir = arrayOf(0,0,0,0)       // Tabla de posibles direcciones
        var num_dir=0;    // Numero de direcciones
        var contador0=0;  // Contador general
        var dir1 = 0;         // Direcciones temporales
        var dir2 = 0;


        // Contando direcciones
        //FROM contador0=0 TO 3;
        (0..3).forEach { contador0 ->
            // Comprueba si se puede avanzar en la dirección de contador0
            if (camino_fantasma(x,y,contador0) && old_dir!=(contador0 xor 1)) {
                dir[num_dir]=contador0; // Si se puede se guarda
                num_dir++;              // Y se incrementa el contador de direcciones posibles
            }
        }

        // Cambia la dirección si no hay ninguna otra
        if (num_dir==0) {
            dir[num_dir]=old_dir xor 1;
            num_dir++;
        }

        // Selecciona la dirección de acuerdo con el nivel
        contador0 = dir[rand(0, num_dir - 1).toInt()];
        // Aleatoriamente y según el nivel elige una dirección u otra
        if (rand(0, 100) < inteligencia[currentGameState.nivel]) {
            if (father.estado == 0) {// Mira en que estado estaba el fantasma
                if (currentGameState.idpaco!=null) {       // Comprueba si paco existe
                    val idpaco = currentGameState.idpaco!!
                    // Es el estado de poder comer y va hacia paco
                    // Mira que distancia es menor, la horizontal o la vertical
                    if (abs(idpaco.x - father.x) > abs(idpaco.y - father.y)) {
                        // Mira si a la derecha o a la izquierda
                        if (idpaco.x > father.x) {
                            dir1 = 1; // Guarda la primera dirección posible
                        } else {
                            dir1 = 0;
                        }
                        if (idpaco.y > father.y) {
                            dir2 = 2; // Guarda una segunda dirección posible
                        } else {
                            dir2 = 3;
                        }
                    } else {            // La diferencia vertical es mayor
                        if (idpaco.y > father.y) {
                            dir1 = 2; // Guarda la primera dirección posible
                        } else {
                            dir1 = 3;
                        }
                        if (idpaco.x > father.x) {
                            dir2 = 1; // Guarda una segunda dirección posible
                        } else {
                            dir2 = 0;
                        }
                    }
                }
            } else {
                val idpaco = currentGameState.idpaco!!
                // Es el estado de ser comido, se aleja de paco
                // Se comprueba que diferencia es mayor, la horizontal o la vertical
                if (abs(idpaco.x - father.x) < abs(idpaco.y - father.y)) {
                    // Y comprueba si es a la izquierda o a la derecha
                    if (idpaco.x > father.x) {
                        dir1 = 0;     // Guarda la primera dirección posible
                    } else {
                        dir1 = 1;
                    }
                    if (idpaco.y > father.y) {
                        dir2 = 3;     // Guarda una segunda dirección posible
                    } else {
                        dir2 = 2;
                    }
                } else {
                    // Mira si hacia arriba o hacia abajo
                    if (idpaco.y > father.y) {
                        dir1 = 3;    // Guarda la primera dirección posible
                    } else {
                        dir1 = 2;
                    }
                    if (idpaco.x > father.x) {
                        dir2 = 0;    // Guarda una segunda dirección posible
                    } else {
                        dir2 = 1;
                    }
                }
            }

            // Si se puede avanzar en la primera dirección posible, la devuelve
            if (old_dir == dir1 && camino_fantasma(x, y, dir1)) {
                return (dir1);
            } else {    // Si no, si se puede avanzar, devuelve la segunda posible dirección
                if (!camino_fantasma(x, y, dir1) && old_dir == dir2 && camino_fantasma(x, y, dir2)) {
                    return (dir2);
                }
            }
            // Si no devuelve la posición en la que sea posible avanzar
            if (camino_fantasma(x, y, dir1) && old_dir != (dir1 xor 1)) {
                contador0 = dir1;
            } else {
                if (camino_fantasma(x, y, dir2) && old_dir != (dir2 xor 1)) {
                    contador0 = dir2;
                }
            }
        }


        return (contador0);
        
        
    }
    
}



/*

//------------------------------------------------------------------------------
// Proceso selecciona_dir
// Controla por donde ira el fantasma
//------------------------------------------------------------------------------

PROCESS selecciona_dir(x,y,old_dir)

PRIVATE
  dir[3];       // Tabla de posibles direcciones
  num_dir=0;    // Numero de direcciones
  contador0=0;  // Contador general
  dir1;         // Direcciones temporales
  dir2;

BEGIN

    // Contando direcciones
    FROM contador0=0 TO 3;
        // Comprueba si se puede avanzar en la dirección de contador0
        IF (camino_fantasma(x,y,contador0) AND old_dir<>(contador0 XOR 1))
            dir[num_dir]=contador0; // Si se puede se guarda
            num_dir++;              // Y se incrementa el contador de direcciones posibles
        END
    END

    // Cambia la dirección si no hay ninguna otra
    IF (num_dir==0)
        dir[num_dir]=old_dir XOR 1;
        num_dir++;
    END

    // Selecciona la dirección de acuerdo con el nivel
    contador0=dir[rand(0,num_dir-1)];
    // Aleatoriamente y según el nivel elige una dirección u otra
    IF (rand(0,100)<inteligencia[nivel])
        IF (father.estado==0) // Mira en que estado estaba el fantasma
            IF (idpaco)       // Comprueba si paco existe
                // Es el estado de poder comer y va hacia paco
                // Mira que distancia es menor, la horizontal o la vertical
                IF (abs(idpaco.x-father.x)>abs(idpaco.y-father.y))
                    // Mira si a la derecha o a la izquierda
                    IF (idpaco.x>father.x)
                        dir1=1; // Guarda la primera dirección posible
                    ELSE
                        dir1=0;
                    END
                    IF (idpaco.y>father.y)
                        dir2=2; // Guarda una segunda dirección posible
                    ELSE
                        dir2=3;
                    END
                ELSE            // La diferencia vertical es mayor
                    IF (idpaco.y>father.y)
                        dir1=2; // Guarda la primera dirección posible
                    ELSE
                        dir1=3;
                    END
                    IF (idpaco.x>father.x)
                        dir2=1; // Guarda una segunda dirección posible
                    ELSE
                        dir2=0;
                    END
                END
            END
        ELSE
            // Es el estado de ser comido, se aleja de paco
            // Se comprueba que diferencia es mayor, la horizontal o la vertical
            IF (abs(idpaco.x-father.x)<abs(idpaco.y-father.y))
                // Y comprueba si es a la izquierda o a la derecha
                IF (idpaco.x>father.x)
                    dir1=0;     // Guarda la primera dirección posible
                ELSE
                    dir1=1;
                END
                IF (idpaco.y>father.y)
                    dir2=3;     // Guarda una segunda dirección posible
                ELSE
                    dir2=2;
                END
            ELSE
                // Mira si hacia arriba o hacia abajo
                IF (idpaco.y>father.y)
                    dir1=3;    // Guarda la primera dirección posible
                ELSE
                    dir1=2;
                END
                IF (idpaco.x>father.x)
                    dir2=0;    // Guarda una segunda dirección posible
                ELSE
                    dir2=1;
                END
            END
        END

        // Si se puede avanzar en la primera dirección posible, la devuelve
        IF (old_dir==dir1 AND camino_fantasma(x,y,dir1))
            RETURN(dir1);
        ELSE    // Si no, si se puede avanzar, devuelve la segunda posible dirección
            IF (NOT camino_fantasma(x,y,dir1) AND old_dir==dir2 AND camino_fantasma(x,y,dir2))
                RETURN(dir2);
            END
        END
        // Si no devuelve la posición en la que sea posible avanzar
        IF (camino_fantasma(x,y,dir1) AND old_dir<>(dir1 XOR 1))
            contador0=dir1;
        ELSE
            IF (camino_fantasma(x,y,dir2) AND old_dir<>(dir2 XOR 1))
                contador0=dir2;
            END
        END
    END
    RETURN(contador0);
END


//------------------------------------------------------------------------------
// Proceso fantasma
// Maneja al fantasma
//------------------------------------------------------------------------------

PROCESS fantasma(x,y,modelo)

PRIVATE
    imagen;         // Contador de gráficos
    num_imagenes;   // Número de imagenes en que da en pantalla
    dir=3;          // Dirección 0=izq. 1=der. 2=abajo 3=arriba

BEGIN
	file=fichero;
    region=1;       // Lo introduce dentro de la región definida

    LOOP
        // Si el fantasma esta en casa entonces da más imagenes
        IF (color(x,y)=="84,252,252" OR estado>0)
            num_imagenes=1;
        ELSE
            num_imagenes=2;
        END

        // Si el fantasma esta en un cruce selecciona un camino
        IF (cuenta_dir(x,y)>2)
            dir=selecciona_dir(x,y,dir); // Devuelve una dirección correcta
        ELSE
            // Si no tiene ningún camino por donde avanza
            IF (NOT camino_fantasma(x,y,dir))
                dir=selecciona_dir(x,y,dir);    // Elige otro camino
            END

            // Aleatoriamente cambia la dirección si se puede
            IF (rand(0,1000)<2)
                dir=selecciona_dir(x,y,dir XOR 1);
            END
        END

        SWITCH (dir)    // Mueve al fantasma
            CASE 0: x-=2; END
            CASE 1: x+=2; END
            CASE 2: y+=2; END
            CASE 3: y-=2; END
        END

        // Comprueba si se sale por los lados de la pantalla
        IF (x<=95) x+=450; END
        IF (x>=546) x-=450; END

        // Comprueba el estado del fantasma
        IF (estado==0)
            graph=modelo+dir;   // Elige un gráfico de la dirección en estado normal
        ELSE
            // El fantasma puede ser comido y parpadea cambiando gráficos
            IF (estado<70 AND estado/7)
                graph=28;
            ELSE
                graph=11;
            END
            estado--;           // Decrementa el contador de estado del fantasma
        END

        // Unicamente muestra la imagen a la velocidad del fantasma
        IF (imagen>=num_imagenes)
            FRAME;
            imagen=0;
        END
        imagen++;               // Incrementa el contador de imagenes
    END
END



//------------------------------------------------------------------------------
// Proceso camino_fantasma
// Controla por donde ira el fantasma
//------------------------------------------------------------------------------

PROCESS camino_fantasma(x,y,dir)

PRIVATE
    n_color;  // Color 10=Punto grande 11=Casa de fantasma 12=Camino 14=Punto

BEGIN
    // Comprueba la dirección
    SWITCH (dir)
        CASE 0: n_color=color(x-2,y); END
        CASE 1: n_color=color(x+2,y); END
        CASE 2: n_color=color(x,y+2); END
        CASE 3: n_color=color(x,y-2); END
    END

    // El fantasma no vuelve a entrar en su casa
    IF (n_color=="84,252,252" AND dir==2 AND color(x,y)=="252,84,84")
        n_color="0,0,0";
    END

    // Retorna verdadero (TRUE) si es un camino correcto
    RETURN(n_color=="84,252,252" OR n_color=="84,252,84" OR n_color=="252,84,84" OR n_color=="252,252,84");
END
//------------------------------------------------------------------------------
// Proceso paco
// Maneja a pacoman
//------------------------------------------------------------------------------

PROCESS paco()

PRIVATE
    imagen;             // Contador de im genes
    velocidad_paco=2;   // Velocidad de pacoman
    incr_x=0;           // Coordenadas relativas
    incr_y=0;
    contador0;          // Contador general
    comido;             // Identificador de fantasma
    grafico_viejo;      // Contenedor temporal de gráficos
    pasos[]=0,1,2,1;    // Tabla de animación
    paso=0;             // Contador de animación
    grafico_inicial=3;  // Gráfico inicial
    puntos_por_comido=200;  // Contador de fantasmas comidos

BEGIN
    idpaco=id;          // Coge el identificador del proceso
    region=1;           // Hace que el proceso aparezca dentro de la región 1
    graph=3;            // Asigna el gráfico y las coordenadas
    file=fichero;
    x=320;
    y=364;
    LOOP
        // Lee teclas
        IF (key(_space))    // Cambia velocidad si se pulsa la barra espaciadora
            velocidad_paco=4;
        ELSE
            velocidad_paco=2;
        END

        // Comprueba si se pulso el cursor de la derecha y puede ir en esa dirección
        IF (key(_right) AND camino_paco(x+2,y))
            grafico_inicial=3;  // Elige el gráfico inicial
            flags=0;
            incr_x=2;           // Pone los incrementos vertical y horizontal
            incr_y=0;
        END

        // Comprueba si se pulso el cursor de la izquierda y puede moverse en esa dirección
        IF (key(_left) AND camino_paco(x-2,y))
            grafico_inicial=3;
            flags=1;
            incr_x=-2;
            incr_y=0;
        END

        // Comprueba si se pulso el cursor de abajo y puede avanzar
        IF (key(_down) AND camino_paco(x,y+2))
            grafico_inicial=6;
            flags=0;
            incr_y=2;
            incr_x=0;
        END

        // Comprueba si se pulso el cursor de arriba y puede avanzar
        IF (key(_up) AND camino_paco(x,y-2))
            grafico_inicial=6;
            flags=2;
            incr_y=-2;
            incr_x=0;
        END

        // Comprueba caminos en la dirección horizontal
        IF (camino_paco(x+incr_x,y))
            x+=incr_x;  // Si se pude mover se mueve
        ELSE
            incr_x=0;   // Detiene el movimiento
            paso=1;     // Y reinicia la animación
        END

        // Comprueba caminos en la dirección horizontal
        IF (camino_paco(x,y+incr_y))
            y+=incr_y;  // Si se pude mover se mueve
        ELSE
            incr_y=0;   // Detiene el movimiento
            paso=1;     // Y reinicia la animación
        END

        // Selecciona gráficos
        graph=grafico_inicial+pasos[paso];

        // Unicamente muestra la pantalla a la velocidad de paco
        IF (imagen>=velocidad_paco)
            FRAME;  // Muestra los gráficos
            imagen=0;
            // Comprueba si ha colisionado con un fantasma
            comido=collision(TYPE fantasma);
            IF (comido)

                // Mira si realmente el fantasma esta cerca
                IF (abs(x-comido.x)>10 OR abs(y-comido.y)>10)
                    comido=0;   // Hace como si no se hubieran tocado
                END

            END
            // Actualiza la animación del gráfico
            IF (incr_x<>0 OR incr_y<>0)
                paso=(paso+1) MOD 4;
            END
        END
        imagen++;   // Incrementa el contador de imagenes

        // Verifica si pacoman ha salido por los lados
        IF (x<=95)
            x+=450; // Hace que aparezca por el otro lado
        END

        IF (x>=546)
            x-=450;
        END

        // Pacoman ha comido un punto
        IF (punto(x,y)=="252,252,84")
            puntos++;       // Incrementa el contador de puntos comidos
            puntuacion+=10; // Incrementa la puntuación
            put(fichero,9,x,y);   // Borra el punto del mapa de durezas
            imagen++;       // Incrementa el contador imagenes
            sound(s_come_punto,128,256);    // Hace el sonido
        END

        // Pacoman ha comido un punto grande
        IF (punto(x,y)=="84,252,84")
            sound(s_come_puntogr,512,256);  // Realiza el sonido
            puntos++;                       // Incrementa el número de puntos comidos
            puntuacion+=50;                 // Incrementa la puntuación
            puntos_por_comido=0;            // Reinicia la variable que guarda
                                            // los puntos que se dan cuando se come a un fantasma
            put(fichero,10,x,y);                  // Quita el punto del mapa de durezas

            // Pone a todos los fantasmas en estado de ser comidos
            contador0=get_id(TYPE fantasma);
            WHILE (contador0)
                contador0.estado=tiempo_capsulas[nivel];
                contador0=get_id(TYPE fantasma);
            END
        END

        // Comprueba si ha tocado un fantasma
        IF (comido)
            IF (comido.estado>0)      // Pacoman se come al fantasma
                sound(s_come_fantasma,512,256);         // Hace el sonido

                // Congela a todos los fantasmas
                contador0=get_id(TYPE fantasma);
                WHILE (contador0)
                    signal(contador0,s_freeze);
                    contador0=get_id(TYPE fantasma);
                END

                grafico_viejo=graph;                    // Guarda el gráfico que tenia
                graph=0;                                // Quita el gráfico
                comido.graph=42+puntos_por_comido;      // Cambia el gráfico del fantasma
                                                        // por el de la puntuación obtenida
                FRAME(1500);                            // Espera a que se den 15 imagenes

                // Vuelve a todos los fantasma al estado en que estaban
                contador0=get_id(TYPE fantasma);
                WHILE (contador0)
                    signal(contador0,s_wakeup);
                    contador0=get_id(TYPE fantasma);
                END
                graph=grafico_viejo;                    // Recupera el gráfico de paco
                ojos(comido.x,comido.y,comido.modelo);  // Crea unos ojos de fantasma
                signal(comido,s_kill);                  // Elimina el fantasma comido
                SWITCH (puntos_por_comido):             // Da la puntuación según los fantasmas comidos
                    CASE 0: puntuacion+=200; END
                    CASE 1: puntuacion+=400; END
                    CASE 2: puntuacion+=800; END
                    CASE 3: puntuacion+=1600; END
                    CASE 4: puntuacion+=3200; END
                END
                puntos_por_comido++;                    // Incrementa para la proxima vez que coma
            ELSE                    // Fantasma se come a pacoman

                // Congela los procesos de todos los fantasmas
                contador0=get_id(TYPE fantasma);
                WHILE (contador0)
                    signal(contador0,s_freeze);
                    contador0=get_id(TYPE fantasma);
                END
                FRAME(1500);        // Espera a que se den 15 imagenes

                // Elimina los procesos de fantasmas, ojos y frutas
                signal(TYPE fantasma,s_kill);
                signal(TYPE ojos,s_kill);
                signal(TYPE fruta,s_kill);
                flags=0;                    // Coloca el gráfico sin ningun espejado
                sound(s_muerte,512,256);    // Realiza el sonido
                FROM contador0=34 TO 41;    // Hace la animación de ser comido
                    graph=contador0;
                    FRAME(400);
                END
                FRAME(800);                 // Espera 8 imagenes de pantalla
                IF (ividas<>0)      // Comprueba si le quedan vidas
                    signal(vidas[ividas],s_kill);
                    ividas--;       // Quita una vida
                    inicio_juego(); // Reinicia el juego
                ELSE
                    ividas--;       // Final del juego
                END
                signal(id,s_kill);
                FRAME;
            END
            comido=0;               // Borra cualquier colisión detectada que hubiera
        END
    END
END



//------------------------------------------------------------------------------
// Proceso camino_paco
// Controla por donde puede ir  pacoman
//------------------------------------------------------------------------------

PROCESS camino_paco(x,y)

PRIVATE
    numero_color;   // Número de color en el mapa de durezas

BEGIN
    numero_color=color(x,y);     // Coge el color del mapa de durezas

    RETURN(numero_color=="84,252,84" OR
           numero_color=="252,84,84" OR
           numero_color=="252,252,84");    // Retorna TRUE si puede avanzar
END
//------------------------------------------------------------------------------
// Proceso punto
// Coge un color del mapa de durezas
//------------------------------------------------------------------------------

PROCESS punto(x,y)

BEGIN
    IF (get_pixel(x,y)=="0,0,0") // Comprueba que el color no sea incorrecto
        RETURN(0);          // Devuelve FALSE (= 0 = falso) porque ese color es valido
    ELSE
        RETURN(color(x,y)); // Retorna el código de color
    END
END
//------------------------------------------------------------------------------
// Proceso color
// Coge un color del mapa de durezas
//------------------------------------------------------------------------------

PROCESS color(x,y)

BEGIN
    // Comprueba si son los lados de la pantalla
    IF ((x<105 OR x>534) AND (y==225 OR y==226))
        RETURN("252,84,84"); // Devuelve un color de camino
    END

    // Devuelve el color del mapa de durezas
    RETURN(map_get_pixel(fichero,2,(x-105)/2,(y-1)/2));
END


//------------------------------------------------------------------------------
// Proceso cuenta_dir
// Cuenta las posibles direcciones del fantasmas
//------------------------------------------------------------------------------

PROCESS cuenta_dir(x,y)

PRIVATE
    dir=0;          // Numero de direcciones
    contador0=0;    // Contador de uso general

BEGIN
    REPEAT          // Va mirando por todas la direcciones
        // Si el camino es posible incrementa el contador
        IF (camino_fantasma(x,y,contador0))
            dir++;  // Contando salidas
        END
    UNTIL (contador0++==3)
    RETURN(dir);    // Devuelve el número de direcciones posibles
END


//------------------------------------------------------------------------------
// Proceso ojos
// Maneja los ojos de los fantasmas ( cuando son comidos)
//------------------------------------------------------------------------------

PROCESS ojos(x,y,modelo)

PRIVATE
    imagen;  // Numero de gráfico

BEGIN
	file=fichero;

    LOOP
        // Comprueba los lados de la pantalla
        IF (x<105) x=105; END
        IF (x>554) x=554; END

        // Selecciona la dirección y el gráfico de acuerdo al color del mapa de caminos
        SWITCH (map_get_pixel(fichero,33,(x-105)/2,(y-1)/2))
            CASE "252,252,84": x-=2; graph=29; END
            CASE "84,252,84": x+=2; graph=30; END
            CASE "252,84,84": y+=2; graph=31; END
            CASE "84,84,252":  y-=2; graph=32; END
            // Si es el color 11 es que ha llegado a casa, quita los ojos y pone un fantasma
            CASE "84,252,252": imagen=0; signal(id,s_kill); fantasma(x,y,modelo); END
        END
        // Solo muestra los gráficos cada cuatro imagens
        //IF ((imagen AND 3)==0) FRAME; END //Así funciona con Div2
        IF ((imagen MOD 3)==0) FRAME; END   //Así funciona con Div GO
        imagen++;
    END
END
//------------------------------------------------------------------------------
// Proceso inicio_juego
// Reinicializa el juego
//------------------------------------------------------------------------------

PROCESS inicio_juego()

PRIVATE
    texto1;     // Identificadores de texto
    texto2;
    texto3;
    contador0;  // Contador de car cter general

BEGIN

    // Para cualquier sonido que estuviera sonando
    stop_sound(all_sound);
    fade_on();  // Encender la pantalla

    // Inicia el sonido de entrada e imprime los texto necesarios
    sound(s_empieza,512,256);
    texto1=write(fuente,320,243,1,"!Preparado!");
    texto2=write(fuente,300,152,1,"Nivel");
    texto3=write_int(fuente,376,152,1,&nivel);

    FRAME(9000);        // Espera

    // Borra los textos
    delete_text(texto1);
    delete_text(texto2);
    delete_text(texto3);

    // Crea a pacoman y a los fantasma
    paco();
    fantasma(320,177,12);
    fantasma(290,223,16);
    fantasma(320,223,20);
    fantasma(352,223,24);
END


//------------------------------------------------------------------------------
// Proceso titulo
// Imprime y mueve los texto
//------------------------------------------------------------------------------

PROCESS titulo(txt)

PRIVATE
    // Coordenadas verticales de titulo
    tabla_grafico[]=50,52,53,54,54,53,52,50,48,47,46,46,47,48;
    contador0=0;    // Contador de uso general

BEGIN
    graph=47;       // Selecciona gráfico de titulo
    file=fichero;
    x=320;          // Pone coordenada horizontal
    z=-10;
    LOOP
        contador0=(contador0+1) MOD 14;

        // Imprímelo dentro de pantalla
        IF (contador0==0) move_text(txt,320,320); END

        // Imprímelo fuera de pantalla lo que hace al texto intermitente
        IF (contador0==7) move_text(txt,320,640); END
        // Mueve el titulo
        y=tabla_grafico[contador0];    // Mueve el titulo
        FRAME;
    END
END


//------------------------------------------------------------------------------
// Proceso fruta
// Maneja las frutas
//------------------------------------------------------------------------------

PROCESS fruta()

PRIVATE
    tiempofruta=100;        // Contador de tiempo

BEGIN
    x=320;                  // Asigna las coordenadas y el gráfico
    y=270;
    graph=g_frutas[nivel];
    file=fichero;
    z=10;                   // Selecciona el gráfico

    // Repite hasta que tiempofruto sea 0
    WHILE (tiempofruta>0)
        tiempofruta--;      // Decrementa tiempo de fruta
        // Fruta comida
        IF (collision(TYPE paco))
            sound(s_come_fruta,512,256);        // Hace el sonido
            tiempofruta=-20;                    // Pone otra pausa
            puntuacion+=valor_frutas[nivel];    // Suma puntuación
            graph=g_valor_frutas[nivel];        // Pon gráfico con puntos
        END
        FRAME;
    END

    // Espera el valor que se puso negativo en tiempofruta
    WHILE (tiempofruta<0)
        tiempofruta++;
        FRAME;
    END
END
//------------------------------------------------------------------------------
// Proceso vida
// Imprime las vidas del marcador
//------------------------------------------------------------------------------

PROCESS vida(x)

BEGIN
    y=14;       // Elige la coordenada vertical
    file=fichero;
    graph=4;    // Selecciona gráfico
    LOOP
        FRAME;
    END
END

//------------------------------------------------------------------------------
// Proceso parpadeante
// Imprime los puntos grandes
//------------------------------------------------------------------------------

PROCESS parpadeante(x,y)

BEGIN
	file=fichero;
    z=10;
    LOOP
        graph=10;   // Imprime el gráfico
        FRAME(300); // Espera
        graph=0;    // No pone ningún gráfico
        FRAME(300); // Espera
    END
END
 */