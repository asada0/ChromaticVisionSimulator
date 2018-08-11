# ChromaticVisionSimulator

This is an open source code of the **“Chromatic Vision Simulator"** Android version 3.0.

<p align="center">
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/cvsimulatoricon.png">
</p>

## What is "Chromatic Vision Simulator"?

- "Chromatic Vision Simulator" is an experience tool which simulates color vision of color vision deficiencies.
- This software makes and shows you a simulated image from built-in camera or image file in real-time.
- Supports "Normal Color Vision," "Protanope," "Deuteranope" and "Tritanope" color deficient types.
- Developed by Kazunori Asada (Ph.D. Medical Science and Media Design) based on the research in color science.

<p align="center">
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/Boot-e3.0a.jpg" width="240"> 
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/Home-e3.0a.jpg" width="240"> 
</p>

## Background
About 5% of men have difficultly distinguishing red and green, or recognizing dark red. Such color deficiency can be classified into three major categories: Protanope / Protanomal, Deuteranope / Deuteranomal and Tritanope / Tritanomal.

Color deficiencies are the inability to tell differences between some colors other people can easily distinguish. People with different types of color vision deficiency perceive colors differently. For example, individuals diagnosed as Protan or Deutan cannot perceive differences between red and green, and pink and cyan.

These color visions are predictable by calculating working of the cone cells that perceive colors to some degree.

"Chromatic Vision Simulator" makes a simulated image of each color vision type of dichromat from built-in camera or image file and shows you how people with a specific type of color deficiency see the world in real-time.

<p align="center">
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/About-e3.0a.jpg" width="240"> 
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/Manual-e3.0a.jpg" width="240"> 
</p>


## Announcement
Announcement of making Open-Source version is here.  
<https://asada.tukusi.ne.jp/cvsimulator/e/announce.html>

## Official Website
Official website of the "Chromatic Vision Simulator" is here.  
<https://asada.tukusi.ne.jp/cvsimulator/e>

## User's Guide
User's Guide of this application is in the official website.  
<http://asada.tukusi.ne.jp/cvsimulator/e/manual.html>

## Requirements

Android  device with Android 5.0 (API level 21) or later and OpenGL ES2.0 or later.

## Cautions

- A simulation algorithm is based on a proposal method of academic research described below.  
> H. Brettel, F.Viénot, and J. D. Mollon: Computerized simulation of color appearance for dichromats, Journal of the Optical Society of America A, vol.14, no.10, pp.2647-2655 (Oct. 1997).   

- Simulated images by this software are predicted images of dichromat vision under specific condition with specific method and not necessarily accurate. There is an individual variation in color vision.
- Although this application has the ability to simulate with the "Simulation Intensity", it is just a function which makes a simple linear interpolation image between the dichromat(the strongest color vision deficiency) simulation and normal vision (original), and it is not intended to simulate the vision of anomalous trichromat(weak color vision deficiency).

## Notes

This application software was developed by Kazunori Asada (Ph.D. of Medical Science and Ph.D. of Media design).

## Acknowledgment
I wish to express my gratitude to Mr. Hirofumi Ukawa and Masataka Matsuda who helped development of the open-source version.

## License
###The MIT License (MIT)  

Copyright 2018 Kazunori Asada

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

