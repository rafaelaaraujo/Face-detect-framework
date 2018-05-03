# Framework for Monitoring Attention and Detection of Expressions

[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badge/)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.png?v=103)](https://opensource.org/licenses/mit-license.php)
[![](https://jitpack.io/v/rafaelaaraujo/Face-detect-framework.svg)](https://jitpack.io/#rafaelaaraujo/Face-detect-framework)

This framework supports biofeedback of application users for the
Android/RemixOS platform. Its main objective is to enable new applications to offer online monitoring functionality of users' attention and expressions. Through the analysis of the data obtained from the camera embedded in a computer system, the framework is able to provide resources that allow the recognition of patterns about the behavior and / or emotions captured during the execution of some activities. 

[International Journal of Computer Applications](https://www.ijcaonline.org/archives/volume158/number5/26906-26906-2017912847)


## Scructural Description

<p align="left">
  <img src="https://github.com/rafaelaaraujo/Face-detect-framework/blob/master/archives/graph.png" width="600"/>
</p>


## Attention Monitoring

  The software is able to identify a person and starts to measure the attention level of the individual, as long as they look towards the camera. Each frame obtained either by using a saved video or in real time through the camera, a face detection algorithm is applied in order to determine the level of attention. 
	Using facial knowledge and looking for distinctive brightness gradients of the eye the attention meter can quickly find, detect eyes, and measure the intermittent rate of a face over several frames. Each identified face is given a rating for attention that varies over time. It starts at 0 and increases according to the attention exhibited by the individual.

<p align="left">
  <img src="https://github.com/rafaelaaraujo/Face-detect-framework/blob/master/archives/attention_sample.png" width="250"/>
  <img src="https://github.com/rafaelaaraujo/Face-detect-framework/blob/master/archives/graph_attention.png" width="250"/>
</p>

## Expression Recognition

  The Fisherfaces algorithm available in the OpenCV library was used in the system so it could learn to recognize expression patterns, which uses ideal machine learning for facial recognition. In Fisherfaces, all pixels of a face, or the image that contains a face is utilized as input of the recognition system. Thus, all images information can be considered in this approach despite having a disadvantage of the high dimensional data increasing computational costs. To solve this problem, the statistical method Principal Component Analysis (PCA) is used in order to have a Dimensionality Reduction. 


<p align="left">
  <img src="https://github.com/rafaelaaraujo/Face-detect-framework/blob/master/archives/expression_sample.jpg" width="500"/>
</p>
