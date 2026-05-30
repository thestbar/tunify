# Tunify

Tunify is an open source and free Guitar Tuner Android application. It is developed implementing the Yin algorithm for pitch detection. You can find more information about the specific algorithm on the relevant [paper](http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf) published by Alain de Cheveigné and Hideki Kawahara in 2001.

Currently I am writing an article were I explain how the algorithm was developed and also I will start the engagement to deploy the application to Android's Play Store. When the article is ready you will be able to find it on my personal website [stavrosbarousis.com](https://stavrosbarousis.com). 

The android code is written in Kotlin.

Feel free to contact me at stavrosbarousis@gmail.com or through the platform from inside my personal website.

## Tech

- Kotlin 2.1 (100% — migrated from Java in May 2026)
- Android Gradle Plugin 8.7 / SDK 36 / Java 21
- AndroidX (ViewBinding, Lifecycle, DataStore, Room with KSP)
- kotlinx.coroutines (Flow for reactive streams)
- Material 3

## Contributing

Please contribute using [Github Flow](https://guides.github.com/introduction/flow/). Create a branch, add commits, and [open a pull request](https://github.com/thestbar/tunify/pulls).

## MIT License

#### Copyright © 2023 Stavros Barousis

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
