[![Build](https://github.com/CompilerStuck/sorting-visualizer/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/CompilerStuck/sorting-visualizer/actions/workflows/maven.yml) 
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
<span class="badge-patreon"><a href="https://patreon.com/CompilerStuck" title="Donate to this project using Patreon"><img src="https://img.shields.io/badge/patreon-donate-yellow.svg" alt="Patreon donate button" /></a></span>



<br />
<div align="center">
  <a href="https://github.com/CompilerStuck/sorting-visualizer">
    <img src="images/logo.png" alt="Logo" width="200" height="200">
  </a>

  <h1 align="center">Sorting Algorithm Visualizer</h3>

  <p align="center">
    Visualizes and Audiolizes Sorting Algorithms!
    <br />
    <a href="insert link to exe"><strong>Try it »</strong></a>
    <br />
    <br />
    <a href="https://youtu.be/9bm-q115OFM">View Demo</a>
    ·
    <a href="https://github.com/CompilerStuck/sorting-visualizer/issues">Report Bug</a>
    ·
    <a href="https://github.com/CompilerStuck/sorting-visualizer/issues">Request Feature</a>
    </p>
</div>
 <br/>
 
This Program Visualizes and Audiolizes Sorting Algorithms. It includes 18 different Sorting Algorithms which can be visualized with 16 differnt Visuals, including two 3D models.
It comes with a user friendly settings menu, letting the user customize what the program should do and how it should look.

<div align="center">
        <a href="insert link to exe">
        <img src="images/demo.png" alt="Program demo">
        </a>
  <p align="center">
</div>

## Downloading and running the Visualizer
You can download the latest version of my Visualizer [here](inset link to release)
* Option 1: Just download the [executable](insert link to exe) and run it
* Option 2: Download the [prebuild JAR file](insert link to jar) and run it with

        java -jar sorting-visualizer-1.3.1.jar

* Option 3: Clone the repository via git and build the code [on your own](https://github.com/CompilerStuck/sorting-visualizer#how-to-build-the-code-for-yourself):

## How to build the Code for yourself
### Clone this repository:

        git clone https://github.com/CompilerStuck/sorting-visualizer.git
        cd sorting-visualizer


### Building the Code:

1. Make sure your installed [JDK](https://jdk.java.net/19/) is at least version 14 and your [environment variables](https://www.baeldung.com/java-home-on-windows-7-8-10-mac-os-x-linux) are set.
2. Open a Terminal in the project folder and execute: 

        .\build


### Running the compiled Code:
* Run following command in a Terminal:

        .\run
        


## Features

* Changing the array size
* Selecting algorithms / Run all algorithms
* Changing the shuffle type
   - Random
   - Reverse
   - Almost Sorted
   - Sorted
* Selecting one of 16 Visualizations - two of those are 3D Models
* Selecting different color gradients and creating your own!
* Showing Measurements during the execution
   -  Sorted Percentage
   - Counting Comparisons
   -  Measuring the estimated Real Time 
   - Counting Swaps
   - Counting Writes to the main Array
   - Counting Writes to possible auxiliary Arrays
* Displaying a comparison table at the end to compare algorithms
* Playing / Muting Sound
* Canceling the current execution

* Sorting Algorithms featured:
   - Quick Sort (Middle Pivot)
   - Quick Sort (Dual Pivot)
   - Merge Sort
   - Shell Sort
   - Selection Sort
   - Double Selection Sort
   - Insertion Sort
   - Heap Sort
   - Gravity Sort
   - Radix Sort (Base 10)
   - Gnome Sort
   - Comb Sort
   - Odd Even Sort
   - Bubble Sort
   - Cocktail Shaker Sort
   - Cycle Sort
   - Counting Sort
   - American Flag Sort


## Known Issues (Work in progess)
* Randomizing >2000 elements doesn't show an animation
* Visuals ImageHorizontal & ImageVertical not included
* BucketSort, BogoSort, PigeonholeSort and TimSort not included

## How to Contribute
1. Clone repo and create a new branch
2. Make changes and test
3. Submit Pull Request with comprehensive description of changes

## Acknowledgements
Thanks to [w0rthy](https://www.youtube.com/c/w0rthyA) and [Musicombo](https://www.youtube.com/c/Musicombo) for their amazing videos and inspiring me to start this project.

Also a big thanks to [@micycle1](https://github.com/micycle1) for his amazing mirror of the processing4 core library, making it available for maven.

## License
[MIT](https://github.com/CompilerStuck/sorting-visualizer/blob/main/LICENSE) - Marcel Mauel, 2022

<br />
<br />

<p align="center">
	<strong>Consider giving my work a :star: to show some :heart:</strong>
	<br/>
	<br/>
	<br/>
	<a href="https://www.buymeacoffee.com/CompilerStuck" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>
</p>


