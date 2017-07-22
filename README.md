
# rezolve
[![CircleCI](https://circleci.com/gh/astronautlabs/rezolve/tree/master.svg?style=shield)](https://circleci.com/gh/astronautlabs/rezolve/tree/master)

This is a Minecraft mod which simplifies automation and provides tools to help you manage complex modded Minecraft bases.

## Can I put this in a modpack?

Yes, you are free to include this mod in a pack or use it on your server without stipulations, but we'd love it if you 
sent us a note about how you've used it so far. Hearing that you are enjoying this mod keeps us motivated to improve it!

## State of the Mod / Balance

This mod is at version 1.1, and is considered beta quality. **Please note** that this mod can be considered **unbalanced**, and 
all recipes and energy costs are expected to change in upcoming releases. We are particularly looking for feedback on the difficulty 
(or lack thereof) of using the mod relative to other mods, because this is intended to be used in a tech-heavy modpack, so it should
ideally fit in with the balance of other common mods like EnderIO, Tinkers Construct, Buildcraft, Mekanism, etc. Please let us know 
your thoughts on how this mod interacts with other mods and how it affects your playthrough.   

## Where do I report bugs?

Please post Github issues for any bugs or crashes you encounter. If we find bugs/crashes on the CurseForge page, we'll try to create Github issues to correspond to them, but really Github is the correct place to file these reports.

## Where do I report feedback?

You can post feedback on the CurseForge page, we'll try to read all of the comments posted there. Feedback can also be posted as a Github issue for now. We may mark your issue as `feedback` and close it but understand that your voice has been heard, and we'll consider your input for future releases. Anyone can see these feedback items by filtering github issues and including closed issues as well.  

## Are you going to post a thread to Minecraft Forum?

Yes, all in good time! For now this mod is in the early stages and we feel it's not quite ready to be presented to the greater community. If you are tinkering with this mod, you are one of our very first users!

## Getting the mod 

**Note**: This mod is still experimental and is, at best, beta-quality. The developers are not responsible for damage to your world files, and the mod lacks a lot of the gameplay balance work that you would expect from a polished mod. **You have been warned.**

That being said, official play-ready builds for this mod can be found on CurseForge:
https://minecraft.curseforge.com/projects/rezolve

## Features 

For recipes, please consult NEI. Note that recipes change depending on whether you have EnderIO loaded.

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

### Unbundler 

You can reconstitute the component items of a bundle using an Unbundler. Unbundlers require RF just like Bundlers, and the cost is the same as creating the bundle. 

### Ethernet Cables

Some machines in this mod must be connected via Ethernet Cable. Ethernet cable works like any other cable, connect it to the Rezolve machine you wish to use. 

### Remote Shell

The Remote Shell lets you access any machine remotely. This includes vanilla machines, Rezolve machines, and machines from other mods. 
Simply connect the Remote Shell to an Ethernet Cable network which is itself connected to a machine you wish to access remotely, and make sure to provide RF energy to the Remote Shell. Note that accessing a remote machine will require an opening RF fee as well as a continuous drain on RF as long as you have the remote machine open. Also, any items you transfer in and out of the machine (including due to automation inputs/outputs) will drain the RF within the Remote Shell and may cause disconnects. You are shown the current RF available within the Remote Shell while you have a remote machine's UI open.

By default the Remote Shell only shows the type of machine and it's position in the world within it's available machines list. You can however name each machine if you have a Database Server connected to your Ethernet Cable network. 

### Database Server

The Database Server provides a common data storage solution for Ethernet networks. It is currently only used by the Remote Shell to store the custom names you assign to machines on an Ethernet Cable network. 

### Security Server

You can also use Ethernet to restrict access to any machine, vanilla, Rezolve, or one from another mod. Simply connect a machine to an Ethernet network and then connect a Security Server block to that Ethernet network. Security Servers do not require RF to work at this time. You can set default rules for machines and players, as well as provide a user level for specific players by typing their Minecraft usernames. This mechanism works by controlling user interface access to the machine on both the client and the server. Note that accessing the contents of a machine may still be possible by an unauthorized user by placing piping/automation blocks nearby the controlled machine. This will be handled in an upcoming version of Rezolve.

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

### Can I get some help with this?

Absolutely! This process should work for any Minecraft developer. Please reach out to us on Github or the Gitter.im room associated with this project and we'll help you with your issue and update this README so other devs have more guidance. 

## Credits 

This mod was started by William Lahti (rezonant on github/twitter, rezonaut on Minecraft, minor contributor to EnderIO), along with design input from the ever-talented Maxillaria. We are looking for more contributors, so please come talk to us in the Gitter.im room for this project if you want to help! Let's make an amazing mod!

We have also looked heavily upon the work done by the Sleepy Trousers team (EnderIO) as well as other mods around the community. 

## License 

This mod, including all its code and graphics, are licensed under the 2-clause BSD license. Feel free to use code you find in this repository for your mod for any purpose, just be sure to attribute us in some reasonable fashion and if you can, let us know how you used the code!