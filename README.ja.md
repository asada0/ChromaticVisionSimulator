# ChromaticVisionSimulator

**「色のシミュレータ」** Android version 3.0のソースコードです。

<p align="center">
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/cvsimulatoricon.png">
</p>

## 「色のシミュレータ」とは?

- 「色のシミュレータ」は、様々な色覚を持つ人の色の見え方をシミュレーションする色覚シミュレーションツールです。
- スマートデバイスの内蔵カメラまたは画像ファイルから得た画像をリアルタイムに変換し、それぞれの色覚タイプ（2色覚）ではどのように色が見えるのか、シミュレーションを行います。
- 1型（P型）、2型（D型）、3型（T型）の2色覚の色の見えをリアルタイムに確認し、一般型（C型）の色の見えと比較することができます。
- 色彩学の理論に基づき、博士（医学・メディアデザイン学）である作者により開発されました。

<p align="center">
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/Boot-j3.0a.jpg" width="240"> 
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/Home-j3.0a.jpg" width="240"> 
</p>

## バックグラウンド
男性の約5%が、赤と緑の色の区別がしにくい、濃い赤が見えにくいなどの色覚的な特徴を持っていると言われています。色覚タイプは主に、一般型（C型）、1型（P型）、2型（D型）、3型（T型）などがあることが知られており、それぞれのタイプや強度によって色の見え方が違います。

1～3型の色覚タイプを持つ人は、一般型の人なら異なって見える多くの色が、同じ色に見えてしまうという特徴があります。例えば、1型や2型の人は、赤と緑、ピンクと水色などの区別が難しい場合があります。

これらの色の見えは、色を感知する錐体細胞の働きを計算することによって、ある程度予測することが可能です。

「色のシミュレータ」は、内蔵カメラまたは画像ファイルから得た画像をリアルタイムに変換し、それぞれの色覚タイプを持つ人がどのように色が見えているのか、シミュレーションを行います。

<p align="center">
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/About-j3.0a.jpg" width="240"> 
<img src="http://asada.tukusi.ne.jp/cvsimulator/my_images/Manual-j3.0a.jpg" width="240"> 
</p>


## アナウンス
オープンソース化に当たってのアナウンスはこちら。
<http://asada.tukusi.ne.jp/cvsimulator/j/announcement.html>

## 公式ウェブサイト
「色のシミュレータ」の公式ウェブサイトはこちら。  
<http://asada.tukusi.ne.jp/cvsimulator/>

## マニュアル
このアプリのマニュアルは公式サイトにあります。
<http://asada.tukusi.ne.jp/cvsimulator/j/manual.html>

## 動作条件

Android 5.0 (API level 21) 以上、 OpenGL ES2.0以上を搭載するAndroidデバイス。  
Android Studio。

## 注意

- 本アプリで使用しているシミュレーションアルゴリズムは、下記の学術論文の提案手法に基づいており、それぞれの色覚タイプを持つ人のうちもっとも強度の、2色覚者が見ているであろう色を高速演算により再現しています。  
> H. Brettel, F.Viénot, and J. D. Mollon: Computerized simulation of color appearance for dichromats, Journal of the Optical Society of America A, vol.14, no.10, pp.2647-2655 (Oct. 1997).   

- 本アプリで表示しているシミュレーション画像は、2色覚者の色の見えを特定の手法と条件下で予測したものであり、必ずしも正確とは限りません。また、色覚には個人差があります。 
- 本アプリには「シミュレーション強度」を指定してシミュレーションを行う機能がありますが、これは、それぞれの色覚タイプを持つ人のうちもっとも強度の、2色覚者が見ているであろう色のシミュレーション画像を100%とし、オリジナル画像を0%として、それらの中間画像をLMS色空間上で線形補間をして求めた画像を表示する機能であり、異常3色覚（弱度の色覚異常）者の色の見えをシミュレーションするものではありません。

## ノート

このアプリは、浅田 一憲（医学・メディアデザイン学）によって開発されました。

## 謝辞
本バージョンの開発にあたって、多くの時間と労力を使ってお手伝いいただいた盟友の鵜川 裕文さん、松田 雅孝さん、そしてテストに協力してくださった全ての友人に感謝申し上げます。

## ライセンス
### The MIT License (MIT)  

Copyright 2018-2019 Kazunori Asada

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## 付録
### 本アプリケーションの色覚シミュレーションで使用している数式 

(1) sRGB(gammaed)からsRGB(linear)へRGB色の変換。 (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{matrix}&space;R_{linear}=\left&space;(&space;\frac{R_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}\\\\&space;G_{linear}=\left&space;(&space;\frac{G_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}\\\\&space;B_{linear}=\left&space;(&space;\frac{B_{device}&plus;0.055}{1.055}&space;\right&space;)^{2.4}&space;\end{matrix}" title="\begin{matrix} R_{linear}=\left ( \frac{R_{device}+0.055}{1.055} \right )^{2.4}\\\\ G_{linear}=\left ( \frac{G_{device}+0.055}{1.055} \right )^{2.4}\\\\ B_{linear}=\left ( \frac{B_{device}+0.055}{1.055} \right )^{2.4} \end{matrix}" />

(2) sRGBからCIEXYZへ色空間の変換。 (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0.4124&space;&&space;0.3576&space;&&space;0.1805\\&space;0.2126&space;&&space;0.7152&space;&&space;0.0722\\&space;0.0193&space;&&space;0.1192&space;&&space;0.9595&space;\end{pmatrix}&space;\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}" title="\begin{pmatrix} X\\ Y\\ Z \end{pmatrix} = \begin{pmatrix} 0.4124 & 0.3576 & 0.1805\\ 0.2126 & 0.7152 & 0.0722\\ 0.0193 & 0.1192 & 0.9595 \end{pmatrix} \begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix}" />

(3) CIEXYZからLMSへ色空間の変換。 (Hunt-Pointer-Estevez, D65照明に正規化)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0.40024&space;&&space;0.70760&space;&&space;-0.08081\\&space;-0.22630&space;&&space;1.16532&space;&&space;0.04570\\&space;0&space;&&space;0&space;&&space;0.91822&space;\end{pmatrix}&space;\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}" title="\begin{pmatrix} L\\ M\\ S \end{pmatrix} = \begin{pmatrix} 0.40024 & 0.70760 & -0.08081\\ -0.22630 & 1.16532 & 0.04570\\ 0 & 0 & 0.91822 \end{pmatrix} \begin{pmatrix} X\\ Y\\ Z \end{pmatrix}" />

- (3') *(2)と(3)を連結した式を用いても良い。*  
<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0.31394&space;&&space;0.63957&space;&&space;0.04652\\&space;0.15530&space;&&space;0.75796&space;&&space;0.08673\\&space;0.01772&space;&&space;0.10945&space;&&space;0.87277&space;\end{pmatrix}&space;\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}" title="\begin{pmatrix} L\\ M\\ S \end{pmatrix} = \begin{pmatrix} 0.31394 & 0.63957 & 0.04652\\ 0.15530 & 0.75796 & 0.08673\\ 0.01772 & 0.10945 & 0.87277 \end{pmatrix} \begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix}" />

(4) LMS色空間において色覚シミュレーションを実行。

シミュレーション後の色は、元の色の色知覚折れ平面への射影として求める。

- (4-1) 1型（P型）2色覚。 (H. Brettel et al., 1997, 改変)  
<img src="https://latex.codecogs.com/gif.latex?\begin{cases}&space;\begin{pmatrix}&space;L_{p}\\&space;M_{p}\\&space;S_{p}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0&space;&&space;1.20800&space;&&space;-0.20797\\&space;0&space;&&space;1&space;&&space;0\\&space;0&space;&&space;0&space;&&space;1&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;S&space;\leq&space;M\\\\&space;\begin{pmatrix}&space;L_{p}\\&space;M_{p}\\&space;S_{p}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;0&space;&&space;1.22023&space;&&space;-0.22020\\&space;0&space;&&space;1&space;&&space;0\\&space;0&space;&&space;0&space;&&space;1&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;S&space;&gt;&space;M&space;\end{cases}" title="\begin{cases} \begin{pmatrix} L_{p}\\ M_{p}\\ S_{p} \end{pmatrix} = \begin{pmatrix} 0 & 1.20800 & -0.20797\\ 0 & 1 & 0\\ 0 & 0 & 1 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } S \leq M\\\\ \begin{pmatrix} L_{p}\\ M_{p}\\ S_{p} \end{pmatrix} = \begin{pmatrix} 0 & 1.22023 & -0.22020\\ 0 & 1 & 0\\ 0 & 0 & 1 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } S &gt; M \end{cases}" />

- (4-2) 2型 （D型）2色覚。 (H. Brettel et al., 1997, 改変)  
<img src="https://latex.codecogs.com/gif.latex?\begin{cases}&space;\begin{pmatrix}&space;L_{d}\\&space;M_{d}\\&space;S_{d}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1&space;&&space;0&space;&&space;0\\&space;0.82781&space;&&space;0&space;&&space;0.17216\\&space;0&space;&&space;0&space;&&space;1&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;S&space;\leq&space;L\\\\&space;\begin{pmatrix}&space;L_{d}\\&space;M_{d}\\&space;S_{d}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1&space;&&space;0&space;&&space;0\\&space;0.81951&space;&&space;0&space;&&space;0.18046\\&space;0&space;&&space;0&space;&&space;1&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;S&space;&gt;&space;L&space;\end{cases}" title="\begin{cases} \begin{pmatrix} L_{d}\\ M_{d}\\ S_{d} \end{pmatrix} = \begin{pmatrix} 1 & 0 & 0\\ 0.82781 & 0 & 0.17216\\ 0 & 0 & 1 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } S \leq L\\\\ \begin{pmatrix} L_{d}\\ M_{d}\\ S_{d} \end{pmatrix} = \begin{pmatrix} 1 & 0 & 0\\ 0.81951 & 0 & 0.18046\\ 0 & 0 & 1 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } S &gt; L \end{cases}" />

- (4-3) 3型（T型）2色覚。 (H. Brettel et al., 1997, 改変)  
<img src="https://latex.codecogs.com/gif.latex?\begin{cases}&space;\begin{pmatrix}&space;L_{t}\\&space;M_{t}\\&space;S_{t}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1&space;&&space;0&space;&&space;0\\&space;0&space;&&space;1&space;&&space;0\\&space;-0.52543&space;&&space;1.52540&space;&&space;0&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;M&space;\leq&space;L\\\\&space;\begin{pmatrix}&space;L_{t}\\&space;M_{t}\\&space;S_{t}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1&space;&&space;0&space;&&space;0\\&space;0&space;&&space;1&space;&&space;0\\&space;-0.87504&space;&&space;1.87503&space;&&space;0&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}&space;&&space;\text{&space;if&space;}&space;M&space;&gt;&space;L&space;\end{cases}" title="\begin{cases} \begin{pmatrix} L_{t}\\ M_{t}\\ S_{t} \end{pmatrix} = \begin{pmatrix} 1 & 0 & 0\\ 0 & 1 & 0\\ -0.52543 & 1.52540 & 0 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } M \leq L\\\\ \begin{pmatrix} L_{t}\\ M_{t}\\ S_{t} \end{pmatrix} = \begin{pmatrix} 1 & 0 & 0\\ 0 & 1 & 0\\ -0.87504 & 1.87503 & 0 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix} & \text{ if } M &gt; L \end{cases}" />

(5) LMSとからCIEXYZへ色空間の変換。 (Hunt-Pointer-Estevez, D65照明に正規化)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;1.85995&space;&&space;-1.12939&space;&&space;0.21990&space;\\&space;0.36119&space;&&space;0.63881&space;&&space;0&space;\\&space;0&space;&&space;0&space;&&space;1.08906&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\&space;M\\&space;S&space;\end{pmatrix}" title="\begin{pmatrix} X\\ Y\\ Z \end{pmatrix} = \begin{pmatrix} 1.85995 & -1.12939 & 0.21990 \\ 0.36119 & 0.63881 & 0 \\ 0 & 0 & 1.08906 \end{pmatrix} \begin{pmatrix} L\\ M\\ S \end{pmatrix}" />

(6) CIEXYZからsRGBへ色空間の変換。 (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;3.2406&space;&&space;-1.5372&space;&&space;-0.4986\\&space;-0.9689&space;&&space;1.8758&space;&&space;0.0415\\&space;0.0557&space;&&space;-0.2040&space;&&space;1.0570&space;\end{pmatrix}&space;\begin{pmatrix}&space;X\\&space;Y\\&space;Z&space;\end{pmatrix}" title="\begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix} = \begin{pmatrix} 3.2406 & -1.5372 & -0.4986\\ -0.9689 & 1.8758 & 0.0415\\ 0.0557 & -0.2040 & 1.0570 \end{pmatrix} \begin{pmatrix} X\\ Y\\ Z \end{pmatrix}" />

- (6') *(5)と(6)を連結した式を用いても良い。*  
<img src="https://latex.codecogs.com/gif.latex?\begin{pmatrix}&space;R_{linear}\\&space;G_{linear}\\&space;B_{linear}&space;\end{pmatrix}&space;=&space;\begin{pmatrix}&space;5.47213&space;&&space;-4.64189&space;&&space;0.16958\\&space;-1.12464&space;&&space;2.29255&space;&&space;-0.16786\\&space;0.02993&space;&&space;-0.19325&space;&&space;1.16339&space;\end{pmatrix}&space;\begin{pmatrix}&space;L\\M\\S&space;\end{pmatrix}" title="\begin{pmatrix} R_{linear}\\ G_{linear}\\ B_{linear} \end{pmatrix} = \begin{pmatrix} 5.47213 & -4.64189 & 0.16958\\ -1.12464 & 2.29255 & -0.16786\\ 0.02993 & -0.19325 & 1.16339 \end{pmatrix} \begin{pmatrix} L\\M\\S \end{pmatrix}" />

(7) sRGB(linear)からsRGB(gammaed)へのRGB色の変換。 (IEC 61966-2-1)

<img src="https://latex.codecogs.com/gif.latex?\begin{matrix}&space;R_{device}=1.055R_{linear}^{\frac{1}{2.4}}-0.055\\\\&space;G_{device}=1.055G_{linear}^{\frac{1}{2.4}}-0.055\\\\&space;B_{device}=1.055B_{linear}^{\frac{1}{2.4}}-0.055&space;\end{matrix}" title="\begin{matrix} R_{device}=1.055R_{linear}^{\frac{1}{2.4}}-0.055\\\\ G_{device}=1.055G_{linear}^{\frac{1}{2.4}}-0.055\\\\ B_{device}=1.055B_{linear}^{\frac{1}{2.4}}-0.055 \end{matrix}" />

(8) シミュレーション強度。  

シミュレーション強度 *k* で調整後の色 *C<sub>k</sub>* は、LMS色空間における元の色 *C<sub>o</sub>* と2色覚シミュレーション色 *C<sub>s</sub>* の線形補間としている。  

<img src="https://latex.codecogs.com/gif.latex?C_{_{k}}=(1-k)C_{o}&plus;kC_{s}\cdots&space;\text{where&space;}0&space;\leq&space;k&space;\leq&space;1" title="C_{_{k}}=(1-k)C_{o}+kC_{s}\cdots \text{where }0 \leq k \leq 1" />