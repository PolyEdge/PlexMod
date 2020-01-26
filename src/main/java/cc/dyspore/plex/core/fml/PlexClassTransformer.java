package cc.dyspore.plex.core.fml;

import cc.dyspore.plex.Plex;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@SuppressWarnings("UnstableApiUsage")
public class PlexClassTransformer implements IClassTransformer {
    public static String descriptor = PlexClassTransformer.class.getName().replace(".", "/");

    public static void onPacket(ChannelHandlerContext context, Packet packet) {
        if (context.channel().isOpen()) {
            try {
                Plex.listeners.onPacket(context, packet);
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (transformedName.equals("net.minecraft.network.NetworkManager")) {
            ClassReader classReader = new ClassReader(bytes);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            for (MethodNode method : classNode.methods) {
                String mappedMethodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, method.name, method.desc);
                if (mappedMethodName.equals("channelRead0") && (method.access & Opcodes.ACC_SYNTHETIC) == 0) {
                    //System.out.println("[plex] transforming channelRead0 " + classNode.name + " " + method.name + " " + method.desc);
                    InsnList insnList = new InsnList();
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, descriptor, "onPacket", method.desc, false));
                    method.instructions.insertBefore(method.instructions.getFirst(), insnList);
                    break;
                }
            }
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        }
        return bytes;
    }
}
