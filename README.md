# PicEvolve

PicEvolve is inspired by Karl Sims' paper [Artificial Evolution for Computer Graphics](http://www.karlsims.com/papers/siggraph91.html). It allows you to interactively evolve populations of symbolic expressions that generate pretty pictures.

### Requirements
* [Maven 3](https://maven.apache.org/)
* [JDK 1.8](http://www.oracle.com/technetwork/java/javase/overview/index.html)

### Installation and Usage
1. Build it
<pre><code>amar@localhost:~/picevolve$ mvn package</code></pre>
2. Run it
<pre><code>amar@localhost:~/picevolve$ java -jar target/picevolve.jar</code></pre>

### Examples
![Hearts](http://i.imgur.com/jTk6IkN.png "Hearts")
<code>(int-xor #0.211523,0.326492,0.568461 (noise (sin (int-xor #0.211523,0.326492,0.568461 (noise (sin (abs Y)) #0.118697,0.250402,0.604694 (abs X)))) (sin #0.118697,0.250402,0.604694) (abs (min (+ (min X (max #0.755957,0.385966,0.996316 #0.096039,0.027084,0.315309)) Y) 0.529319073657668))))</code>

***

![Squares](http://i.imgur.com/vQ8Tuly.png "Squares")
<code>(noise (log (round (abs X))) (blur (noise (log (int-or (noise (log (float-and 0.2760312450829979 0.24342386143789896)) (int-or X X) (expt (float-and #0.156553,0.868430,0.043015 0.9888737625714226))) #0.373962,0.543220,0.614090)) (int-or (* Y #0.327278,0.928108,0.840876) X) (int-or (+ (float-or 0.3427924346752509 (int-or X (float-and Y #0.514343,0.852054,0.550241))) (int-xor Y 0.5359102834334905)) X))) (int-or #0.772529,0.672399,0.376554 Y))</code>

***

![Miro](http://i.imgur.com/Co0ctc4.png "Miro")
<code>(int-xor (* (/ Y X) 0.9695967223196541) (noise (abs Y) #0.712124,0.946591,0.629391 X))</code>
