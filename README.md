# Framework for Monitoring Attention and Detection of Expressions

[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badge/)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.png?v=103)](https://opensource.org/licenses/mit-license.php)
[![](https://jitpack.io/v/rafaelaaraujo/Face-detect-framework.svg)](https://jitpack.io/#rafaelaaraujo/Face-detect-framework)

This framework supports biofeedback of application users for the
Android/RemixOS platform. Its main objective is to enable new applications to offer online monitoring functionality of users' attention and expressions. Through the analysis of the data obtained from the camera embedded in a computer system, the framework is able to provide resources that allow the recognition of patterns about the behavior and / or emotions captured during the execution of some activities. 

[International Journal of Computer Applications](https://www.ijcaonline.org/archives/volume158/number5/26906-26906-2017912847)


## Scructural Description

  The abstract class CameraCallBack defines an application template enforcing the classes that extend it to implement their methods and the contracts defined in the CvCameraViewListener interface. These methods are responsible for configuring and retrieving the data from the device’s camera. Likewise, if it is necessary to create a new class to receive data from the camera, the class CameraCallBack just need to be extended, the same way it is done in the class FaceDetect, which is responsible to identify the face, set the attention level and identify the user's emotion. In this relationship, the concepts of inheritance and polymorphism from object-oriented programming are applied. These concepts allow attributes and methods to be shared by classes and implemented in their own way, making it easier to insert new features that also analyze the data obtained from the camera.

<p align="left">
  <img src="https://github.com/rafaelaaraujo/Face-detect-framework/blob/master/graph.png" width="600"/>
</p>


## Attention Monitoring

  The software is able to identify a person and starts to measure the attention level of the individual, as long as they look towards the camera. Each frame obtained either by using a saved video or in real time through the camera, a face detection algorithm is applied in order to determine the level of attention. 
	Using facial knowledge and looking for distinctive brightness gradients of the eye the attention meter can quickly find, detect eyes, and measure the intermittent rate of a face over several frames. Each identified face is given a rating for attention that varies over time. It starts at 0 and increases according to the attention exhibited by the individual.

<p align="left">
  <img src="https://github.com/rafaelaaraujo/Face-detect-framework/blob/master/attention_sample.png" width="250"/>
  <img src="https://github.com/rafaelaaraujo/Face-detect-framework/blob/master/graph_attention.png" width="250"/>
</p>

## Expression Recognition

  The Fisherfaces algorithm available in the OpenCV library was used in the system so it could learn to recognize expression patterns, which uses ideal machine learning for facial recognition. In Fisherfaces, all pixels of a face, or the image that contains a face is utilized as input of the recognition system. Thus, all images information can be considered in this approach despite having a disadvantage of the high dimensional data increasing computational costs. To solve this problem, the statistical method Principal Component Analysis (PCA) is used in order to have a Dimensionality Reduction. 


<p align="left">
  <img src="https://github.com/rafaelaaraujo/Face-detect-framework/blob/master/expression_sample.png" width="500"/>
</p>
