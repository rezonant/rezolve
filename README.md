
# rezolve
[![CircleCI](https://circleci.com/gh/astronautlabs/rezolve/tree/master.svg?style=shield)](https://circleci.com/gh/astronautlabs/rezolve/tree/master)

This is a Minecraft mod which simplifies automation and provides tools to help you manage complex modded Minecraft bases.

## Getting the mod 

**Note**: This mod is still experimental and is, at best, alpha-quality. The developers are not responsible for damage to your world files, and the mod lacks a lot of the gameplay balance work that you would expect from a polished mod. **You have been warned.**

That being said, official play-ready builds for this mod can be found on CurseForge:
https://minecraft.curseforge.com/projects/rezolve


## Features 

### Bundler 

You can use this machine to bundle a set of items into a single item. This can be used for automation/flow control purposes or for inventory storage purposes. The bundler requires RF energy, and the bigger the bundle being made, the more RF is required. To create a bundle, you must supply the bundler with a Bundle Pattern (see Bundle Pattern Builder below).

After you have made a bundle, you can then reduce the bundle to its constituent items via the Unbundler (see below).

Bundles can be nested, allowing for arbitrarily deep storage. However, this exponentially increases the RF cost. The cost to bundle an item is the same as the cost to unbundle an item.

The formula for the RF cost of bundling/unbundling is as follows: 

```
(ItemCount * 100) * 2^(MaxBundleDepth)
```
Where `ItemCount` is the total number of items involved in the bundle (including all nested items, and including 1 point per Bundle item), and `MaxBundleDepth` is the deepest level of Bundle involved in the bundle you are creating. This makes very deep bundles very expensive.

### Bundle Pattern Builder 

Use this machine to create patterns which can be slotted into Bundlers. The Bundle Pattern Builder offers a 9x9 ghost inventory for specifying the component items of a bundle. You must then insert a Blank Bundle Pattern, and then you can extract a Bundle Pattern encoded with the items you specified. You can also color-code the bundle pattern which may help with mod compatibility and sorting/organization- to do so, insert a dye into the dye slot. The resulting bundle will be colored based on the dye.

The Bundle Pattern Builder requires RF to work, but it is a small static fee of 100 RF. This fee does not change regardless of how complex a bundle is. The item tooltip for a bundle pattern indicates what items are included, including any nested bundles.

### Machine: Unbundler 

You can reconstitute the component items of a bundle using an Unbundler. Unbundlers require RF just like Bundlers, and the cost is the same as creating the bundle. 

## Developers

### Getting Started

Add this folder within an Eclipse workspace folder and run:

```
gradlew setupDecompWorkspace
```

Install Gradle Buildship in Eclipse via Help -> Install New Software.
You can now `Import` the folder as a `Gradle Project` within Eclipse. 

To build the mod and run it within Minecraft use the `runClient` Gradle task via command line or within Eclipse:
```
gradlew runClient
```

If you wish to test alongside compatible mods, you can copy the mods from the `mods` folder into `run/mods`.

### Maven

All versions of this mod are available in a Maven repository hosted on PackageCloud:
https://packagecloud.io/rezonant/rezolve

You can depend on this repository in your own mod's `build.gradle` to include it in your development environment.

### Troubleshooting
If at any point you are missing libraries in your IDE, or you've run into problems you can run "gradlew --refresh-dependencies" to refresh the local cache. "gradlew clean" to reset everything {this does not effect your code} and then start the processs again.

Should it still not work, 
Refer to #ForgeGradle on EsperNet for more information about the gradle environment.

Tip:
If you do not care about seeing Minecraft's source code you can replace "setupDecompWorkspace" with one of the following:
"setupDevWorkspace": Will patch, deobfusicated, and gather required assets to run minecraft, but will not generated human readable source code.
"setupCIWorkspace": Same as Dev but will not download any assets. This is useful in build servers as it is the fastest because it does the least work.

Tip:
When using Decomp workspace, the Minecraft source code is NOT added to your workspace in a editable way. Minecraft is treated like a normal Library. Sources are there for documentation and research purposes and usually can be accessed under the 'referenced libraries' section of your IDE.
