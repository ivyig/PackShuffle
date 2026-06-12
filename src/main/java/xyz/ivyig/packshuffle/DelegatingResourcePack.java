package xyz.ivyig.packshuffle;

import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.registry.VersionedIdentifier;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class DelegatingResourcePack extends DirectoryResourcePack {
    private ResourcePack delegate;
    private static byte[] iconBytes;

    static {
        try (InputStream is = DelegatingResourcePack.class.getResourceAsStream("/PackShuffle.png")) {
            if (is != null) {
                iconBytes = is.readAllBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DelegatingResourcePack(ResourcePackInfo info, Path root) {
        super(info, root);
    }

    public void setDelegate(ResourcePack delegate) {
        this.delegate = delegate;
    }

    public ResourcePack getDelegate() {
        return this.delegate;
    }

    @Override
    public InputSupplier<InputStream> openRoot(String... segments) {
        if (segments != null && segments.length == 1 && "pack.png".equals(segments[0])) {
            if (iconBytes != null) {
                return () -> new java.io.ByteArrayInputStream(iconBytes);
            }
        }
        return delegate != null ? delegate.openRoot(segments) : null;
    }

    @Override
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        return delegate != null ? delegate.open(type, id) : null;
    }

    @Override
    public void findResources(ResourceType type, String namespace, String path, ResultConsumer consumer) {
        if (delegate != null) {
            delegate.findResources(type, namespace, path, consumer);
        }
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return delegate != null ? delegate.getNamespaces(type) : Set.of();
    }

    @Override
    public <T> T parseMetadata(ResourceMetadataSerializer<T> serializer) throws IOException {
        return delegate != null ? delegate.parseMetadata(serializer) : null;
    }

    @Override
    public Optional<VersionedIdentifier> getKnownPackInfo() {
        return delegate != null ? delegate.getKnownPackInfo() : Optional.empty();
    }

    @Override
    public void close() {
    }
}
