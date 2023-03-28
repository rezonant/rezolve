//package com.astronautlabs.mc.rezolve.core;
//
//import java.util.Map;
//
//import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
//import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
//
//@MCVersion("1.10.2")
//@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
//public class RezolveLoadingPlugin implements IFMLLoadingPlugin {
//
//	@Override
//	public String[] getASMTransformerClass() {
//		System.out.println("RezolveLoadingPlugin has been loaded!");
//		return new String[] {
//			EntityPlayerTransformer.class.getName()
//		};
//	}
//
//	@Override
//	public String getModContainerClass() {
//		return RezolveModContainer.class.getName();
//	}
//
//	@Override
//	public String getSetupClass() {
//		return null;
//	}
//
//	@Override
//	public void injectData(Map<String, Object> data) {
//	}
//
//	@Override
//	public String getAccessTransformerClass() {
//		return null;
//	}
//
//}
