package io.left.rightmesh.libcbor.parser.items;

/**
 * @author Lucien Loiseau on 28/01/19.
 */
public interface ItemFactory<T extends ParseableItem> {
    T createItem();
}
