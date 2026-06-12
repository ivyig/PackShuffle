package xyz.ivyig.packshuffle;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class DelegatingResourcePack extends PathPackResources {
    private PackResources delegate;
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

    public DelegatingResourcePack(PackLocationInfo locationInfo, Path root) {
        super(locationInfo, root);
    }

    public void setDelegate(PackResources delegate) {
        this.delegate = delegate;
    }

    public PackResources getDelegate() {
        return this.delegate;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... segments) {
        if (segments != null && segments.length == 1 && "pack.png".equals(segments[0])) {
            if (iconBytes != null) {
                return () -> new java.io.ByteArrayInputStream(iconBytes);
            }
        }
        return delegate != null ? delegate.getRootResource(segments) : null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        return delegate != null ? delegate.getResource(type, id) : null;
    }

    @Override
    public void listResources(PackType type, String namespace, String path, PackResources.ResourceOutput output) {
        if (delegate != null) {
            delegate.listResources(type, namespace, path, output);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return delegate != null ? delegate.getNamespaces(type) : Set.of();
    }

    @Override
    public <T> T getMetadataSection(MetadataSectionType<T> type) throws IOException {
        return delegate != null ? delegate.getMetadataSection(type) : null;
    }

    @Override
    public Optional<KnownPack> knownPackInfo() {
        return delegate != null ? delegate.knownPackInfo() : Optional.empty();
    }

    @Override
    public void close() {
    }
}
