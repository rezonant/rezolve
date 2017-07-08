
# rezolve

This is a Minecraft mod which simplifies automation and provides tools to help you manage complex modded Minecraft bases.

## Development: Getting Started

Add this folder within an Eclipse workspace folder and run:

```
gradlew setupDecompWorkspace
```

You can now `Import` the folder as an `Existing Project` within Eclipse. 

To build the mod and run it within Minecraft:

```
gradlew runClient
```

If you wish to test alongside compatible mods, you can copy the mods from the `mods` folder into `run/mods`.

## Troubleshooting

If at any point you are missing libraries in your IDE, or you've run into problems you can run "gradlew --refresh-dependencies" to refresh the local cache. "gradlew clean" to reset everything {this does not effect your code} and then start the processs again.

Should it still not work, 
Refer to #ForgeGradle on EsperNet for more information about the gradle environment.

Tip:
If you do not care about seeing Minecraft's source code you can replace "setupDecompWorkspace" with one of the following:
"setupDevWorkspace": Will patch, deobfusicated, and gather required assets to run minecraft, but will not generated human readable source code.
"setupCIWorkspace": Same as Dev but will not download any assets. This is useful in build servers as it is the fastest because it does the least work.

Tip:
When using Decomp workspace, the Minecraft source code is NOT added to your workspace in a editable way. Minecraft is treated like a normal Library. Sources are there for documentation and research purposes and usually can be accessed under the 'referenced libraries' section of your IDE.
