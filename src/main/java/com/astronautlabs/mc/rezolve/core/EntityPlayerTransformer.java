package com.astronautlabs.mc.rezolve.core;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class EntityPlayerTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if ("net.minecraft.entity.player.EntityPlayer".equals(name))
			return this.patchEntityPlayer(name, transformedName, basicClass);
		if ("net.minecraft.entity.player.EntityPlayerMP".equals(name))
			return this.patchEntityPlayer(name, transformedName, basicClass);
		
		
		return basicClass;
	}
	
	private byte[] patchEntityPlayer(String name, String transformedName, byte[] bytes) {

		System.out.println("REZOLVE Transforming EntityPlayer class with name: "+name+" -- Transformed name: "+transformedName);
		
		String targetMethodName = "onUpdate";
		String targetInvokedMethodName = "canInteractWith";
		String targetInvokedMethodOwner = "net/minecraft/inventory/Container";
		String targetInvokedMethodDesc = "(Lnet/minecraft/entity/player/EntityPlayer;)Z";
		//set up ASM class manipulation stuff. Consult the ASM docs for details
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		Iterator<MethodNode> methods = classNode.methods.iterator();
		while(methods.hasNext()) {
			MethodNode m = methods.next();
			int target_index = -1;
	
			System.out.println("REZOLVE: EntityPlayer method "+m.name);
			//Check if this is doExplosionB and it's method signature is ()V which means that it takes no args ("()") and returns a void ("V")
			if (!m.name.equals(targetMethodName) || !m.desc.equals("()V"))
				continue;
		
			System.out.println("********* Inside target method!");
	
			MethodInsnNode targetNode = null;
	
			@SuppressWarnings("unchecked")
			Iterator<AbstractInsnNode> iter = m.instructions.iterator();
	
			int index = -1;
	
			//Loop over the instruction set and find the instruction FDIV which does the division of 1/explosionSize
			while (iter.hasNext())
			{
				index++;
				AbstractInsnNode node = iter.next();

				// mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/player/EntityPlayer", "openContainer", "Lnet/minecraft/inventory/Container;");
				// mv.visitVarInsn(ALOAD, 0); // loading the player onto the stack
				// // stack is [container, player]
				// mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/inventory/Container", "canInteractWith", "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false);
				
				//Found it! save the index location and node for this instruction
				if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
					
					MethodInsnNode invokeNode = (MethodInsnNode)node;
				
					System.out.println("Looking at an invocation to "+invokeNode.owner+"::"+invokeNode.name+"::"+invokeNode.desc);
					boolean matches = 
						targetInvokedMethodOwner.equals(invokeNode.owner) 
						&& targetInvokedMethodName.equals(invokeNode.name)
						&& targetInvokedMethodDesc.equals(invokeNode.desc)
					;
					
					if (matches) {	
						System.out.println("Identified canInteractWith instruction...");
						targetNode = invokeNode;
						target_index = index;
						break;
					}
				}
			}

			if (targetNode == null) {
				throw new RuntimeException("Could not locate canInteractWith instruction!");
			} else {
				
				System.out.println("Patching canInteractWith instruction...");
				m.instructions.insertBefore(
					targetNode,
					new MethodInsnNode(Opcodes.INVOKESTATIC, 
						"com/astronautlabs/mc/rezolve/RezolveMod", 
						"canInteractWith", 
						Type.getMethodDescriptor(
							Type.BOOLEAN_TYPE, 
							Type.getObjectType("java/lang/Object"), 
							Type.getObjectType("java/lang/Object")
						), 
						false
					)
				);
				
				m.instructions.remove(targetNode);
				
				System.out.println("[Rezolve/EntityPlayer.onUpdate] Patching Complete!");
			}
			
			break;
		}

		//ASM specific for cleaning up and returning the final bytes for JVM processing.
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
		
	}

}
