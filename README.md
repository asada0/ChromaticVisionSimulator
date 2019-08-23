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
<http://asada.tukusi.ne.jp/cvsimulator/e/announcement.html>

## Official Website
Official website of the "Chromatic Vision Simulator" is here.  
<http://asada.tukusi.ne.jp/cvsimulator/e>

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
### The MIT License (MIT)  

Copyright 2018-2019 Kazunori Asada

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Appendix
### Mathematical Expressions for Color Vision Simulation in this application 

(1) Convert RGB color from sRGB(gammaed) to sRGB(linear). (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{matrix}&space;R_{linear}=\left&space;(&space;\frac{R_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}\\\\&space;G_{linear}=\left&space;(&space;\frac{G_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}\\\\&space;B_{linear}=\left&space;(&space;\frac{B_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}&space;\end{matrix}" title="\begin{matrix} R_{linear}=\left ( \frac{R_{device}+0.055}{1.055} \right )^{2.4}\\\\ G_{linear}=\left ( \frac{G_{device}+0.055}{1.055} \right )^{2.4}\\\\ B_{linear}=\left ( \frac{B_{device}+0.055}{1.055} \right )^{2.4} \end{matrix}" />

(2) Convert color space from sRGB to CIEXYZ. (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0.4124&space;&&space;0.3576&space;&&space;0.1805\\&space;0.2126&space;&&space;0.7152&space;&&space;0.0722\\&space;0.0193&space;&&space;0.1192&space;&&space;0.9595&space;\end{pmatrix}&space;\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}" title="\begin{pmatrix} X\\ Y\\ Z \end{pmatrix} = \begin{pmatrix} 0.4124 & 0.3576 & 0.1805\\ 0.2126 & 0.7152 & 0.0722\\ 0.0193 & 0.1192 & 0.9595 \end{pmatrix} \begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix}" />

(3) Convert color space from CIEXYZ to LMS. (Hunt-Pointer-Estevez, Normalized to D65)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0.40024&space;&&space;0.70760&space;&&space;-0.08081\\&space;-0.22630&space;&&space;1.16532&space;&&space;0.04570\\&space;0&space;&&space;0&space;&&space;0.91822&space;\end{pmatrix}&space;\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}" title="\begin{pmatrix} L\\ M\\ S \end{pmatrix} = \begin{pmatrix} 0.40024 & 0.70760 & -0.08081\\ -0.22630 & 1.16532 & 0.04570\\ 0 & 0 & 0.91822 \end{pmatrix} \begin{pmatrix} X\\ Y\\ Z \end{pmatrix}" />

- (3') *You can use a concatenation expression of (2) and (3).*  
<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0.31394&space;&&space;0.63957&space;&&space;0.04652\\&space;0.15530&space;&&space;0.75796&space;&&space;0.08673\\&space;0.01772&space;&&space;0.10945&space;&&space;0.87277&space;\end{pmatrix}&space;\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}" title="\begin{pmatrix} L\\ M\\ S \end{pmatrix} = \begin{pmatrix} 0.31394 & 0.63957 & 0.04652\\ 0.15530 & 0.75796 & 0.08673\\ 0.01772 & 0.10945 & 0.87277 \end{pmatrix} \begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix}" />

(4) Color Vision Simulation on LMS color space.

Simulated color is calculate as projected orginal color to the color perception half plane.

- (4-1) for Protanope. (H. Brettel et al., 1997, Modified)  
<img src="https://latex.codecogs.com/gif.latex?\begin{cases}&space;\begin{pmatrix}&space;L_{p}\\&space;M_{p}\\&space;S_{p}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0&space;&&space;1.20800&space;&&space;-0.20797\\&space;0&space;&&space;1&space;&&space;0\\&space;0&space;&&space;0&space;&&space;1&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;S&space;\leq&space;M\\\\&space;\begin{pmatrix}&space;L_{p}\\&space;M_{p}\\&space;S_{p}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0&space;&&space;1.22023&space;&&space;-0.22020\\&space;0&space;&&space;1&space;&&space;0\\&space;0&space;&&space;0&space;&&space;1&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;S&space;&gt;&space;M&space;\end{cases}" title="\begin{cases} \begin{pmatrix} L_{p}\\ M_{p}\\ S_{p} \end{pmatrix} = \begin{pmatrix} 0 & 1.20800 & -0.20797\\ 0 & 1 & 0\\ 0 & 0 & 1 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } S \leq M\\\\ \begin{pmatrix} L_{p}\\ M_{p}\\ S_{p} \end{pmatrix} = \begin{pmatrix} 0 & 1.22023 & -0.22020\\ 0 & 1 & 0\\ 0 & 0 & 1 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } S &gt; M \end{cases}" />

- (4-2) for Deuteranope. (H. Brettel et al., 1997, Modified)  
<img src="https://latex.codecogs.com/gif.latex?\begin{cases}&space;\begin{pmatrix}&space;L_{d}\\&space;M_{d}\\&space;S_{d}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1&space;&&space;0&space;&&space;0\\&space;0.82781&space;&&space;0&space;&&space;0.17216\\&space;0&space;&&space;0&space;&&space;1&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;S&space;\leq&space;L\\\\&space;\begin{pmatrix}&space;L_{d}\\&space;M_{d}\\&space;S_{d}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1&space;&&space;0&space;&&space;0\\&space;0.81951&space;&&space;0&space;&&space;0.18046\\&space;0&space;&&space;0&space;&&space;1&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;S&space;&gt;&space;L&space;\end{cases}" title="\begin{cases} \begin{pmatrix} L_{d}\\ M_{d}\\ S_{d} \end{pmatrix} = \begin{pmatrix} 1 & 0 & 0\\ 0.82781 & 0 & 0.17216\\ 0 & 0 & 1 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } S \leq L\\\\ \begin{pmatrix} L_{d}\\ M_{d}\\ S_{d} \end{pmatrix} = \begin{pmatrix} 1 & 0 & 0\\ 0.81951 & 0 & 0.18046\\ 0 & 0 & 1 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } S &gt; L \end{cases}" />

- (4-3) for Tritanope. (H. Brettel et al., 1997, Modified)  
<img src="https://latex.codecogs.com/gif.latex?\begin{cases}&space;\begin{pmatrix}&space;L_{t}\\&space;M_{t}\\&space;S_{t}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1&space;&&space;0&space;&&space;0\\&space;0&space;&&space;1&space;&&space;0\\&space;-0.52543&space;&&space;1.52540&space;&&space;0&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;M&space;\leq&space;L\\\\&space;\begin{pmatrix}&space;L_{t}\\&space;M_{t}\\&space;S_{t}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1&space;&&space;0&space;&&space;0\\&space;0&space;&&space;1&space;&&space;0\\&space;-0.87504&space;&&space;1.87503&space;&&space;0&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;M&space;&gt;&space;L&space;\end{cases}" title="\begin{cases} \begin{pmatrix} L_{t}\\ M_{t}\\ S_{t} \end{pmatrix} = \begin{pmatrix} 1 & 0 & 0\\ 0 & 1 & 0\\ -0.52543 & 1.52540 & 0 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } M \leq L\\\\ \begin{pmatrix} L_{t}\\ M_{t}\\ S_{t} \end{pmatrix} = \begin{pmatrix} 1 & 0 & 0\\ 0 & 1 & 0\\ -0.87504 & 1.87503 & 0 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } M &gt; L \end{cases}" />

(5) Convert color space from LMS to CIEXYZ. (Hunt-Pointer-Estevez, Normalized to D65)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1.85995&space;&&space;-1.12939&space;&&space;0.21990&space;\\&space;0.36119&space;&&space;0.63881&space;&&space;0&space;\\&space;0&space;&&space;0&space;&&space;1.08906&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}" title="\begin{pmatrix} X\\ Y\\ Z \end{pmatrix} = \begin{pmatrix} 1.85995 & -1.12939 & 0.21990 \\ 0.36119 & 0.63881 & 0 \\ 0 & 0 & 1.08906 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix}" />

(6) Convert color space from CIEXYZ to sRGB. (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;3.2406&space;&&space;-1.5372&space;&&space;-0.4986\\&space;-0.9689&space;&&space;1.8758&space;&&space;0.0415\\&space;0.0557&space;&&space;-0.2040&space;&&space;1.0570&space;\end{pmatrix}&space;\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}" title="\begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix} = \begin{pmatrix} 3.2406 & -1.5372 & -0.4986\\ -0.9689 & 1.8758 & 0.0415\\ 0.0557 & -0.2040 & 1.0570 \end{pmatrix} \begin{pmatrix} X\\ Y\\ Z \end{pmatrix}" />

- (6') *You can use a concatenation expression of (5) and (6).*  
<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;5.47213&space;&&space;-4.64189&space;&&space;0.16958\\&space;-1.12464&space;&&space;2.29255&space;&&space;-0.16786\\&space;0.02993&space;&&space;-0.19325&space;&&space;1.16339&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\M\\S&space;\end{pmatrix}" title="\begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix} = \begin{pmatrix} 5.47213 & -4.64189 & 0.16958\\ -1.12464 & 2.29255 & -0.16786\\ 0.02993 & -0.19325 & 1.16339 \end{pmatrix} \begin{pmatrix} L\\M\\S \end{pmatrix}" />

(7) Convert RGB color from sRGB(linear) to sRGB(gammaed). (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{matrix}&space;R_{device}=1.055R_{linear}^{\frac{1}{2.4}}-0.055\\\\&space;G_{device}=1.055G_{linear}^{\frac{1}{2.4}}-0.055\\\\&space;B_{device}=1.055B_{linear}^{\frac{1}{2.4}}-0.055&space;\end{matrix}" title="\begin{matrix} R_{device}=1.055R_{linear}^{\frac{1}{2.4}}-0.055\\\\ G_{device}=1.055G_{linear}^{\frac{1}{2.4}}-0.055\\\\ B_{device}=1.055B_{linear}^{\frac{1}{2.4}}-0.055 \end{matrix}" />

(8) Simulation Intensity.  

Color *C<sub>k</sub>* after adjustment with simulation intensity *k* is a linear interpolation of the original color *C<sub>o</sub>* and the dichromatic simulation color *C<sub>s</sub>* in the LMS color space.

<img src="https://latex.codecogs.com/gif.latex?C_{_{k}}=(1-k)C_{o}&plus;kC_{s}\cdots&space;\text{where&space;}0&space;\leq&space;k&space;\leq&space;1" title="C_{_{k}}=(1-k)C_{o}+kC_{s}\cdots \text{where }0 \leq k \leq 1" />