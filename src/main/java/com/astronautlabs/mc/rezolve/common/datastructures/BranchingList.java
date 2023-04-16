package com.astronautlabs.mc.rezolve.common.datastructures;

import java.util.*;

public class BranchingList<T> implements List<T> {

	public BranchingList() {
		this.localItems = new ArrayList<T>();
	}

	private BranchingList(BranchingList parent) {
		this();
		this.mParent = parent;
	}

	public BranchingList<T> branch() {
		return new BranchingList<T>(this);
	}

	private BranchingList<T> mParent;
	private ArrayList<T> localItems;

	public int size() {
		return this.localItems.size() + (this.mParent != null ? this.mParent.size() : 0);
	}

	@Override
	public void clear() {
		this.localItems.clear();
	}

	@Override
	public boolean add(T t) {
		return this.localItems.add(t);
	}

	@Override
	public Object[] toArray() {
		return this.toArray(new Object[this.size()]);
	}

	@Override
	public T get(int index) {
		if (index < this.localItems.size())
			return this.localItems.get(index);

		if (this.mParent != null)
			return this.mParent.get(index - this.localItems.size());

		return null;
	}

	@Override
	public T set(int index, T element) {
		return this.localItems.set(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return this.localItems.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		return this.localItems.addAll(index, c);
	}

	@Override
	public boolean contains(Object o) {
		if (this.localItems.contains(o))
			return true;

		if (this.mParent != null && this.mParent.contains(o))
			return true;

		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (this.localItems.containsAll(c))
			return true;

		if (this.mParent != null && this.mParent.containsAll(c))
			return true;

		return false;
	}

	@Override
	public boolean isEmpty() {
		return this.localItems.isEmpty()
			&& (this.mParent == null || this.mParent.isEmpty());
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		for (int i = 0, max = this.size(), localSize = this.localItems.size(); i < max; ++i) {
			if (i < localSize)
				a[i] = (T1)this.localItems.get(i);
			else if (this.mParent != null)
				a[i] = (T1)this.mParent.get(i - localSize);
			else
				break;
		}

		return a;
	}

	@Override
	public boolean remove(Object o) {
		return this.localItems.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.localItems.removeAll(c);
	}

	@Override
	public T remove(int index) {
		return this.localItems.remove(index);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.localItems.retainAll(c);
	}

	@Override
	public int indexOf(Object o) {
		int index = this.localItems.indexOf(o);

		if (index >= 0)
			return index;

		if (this.mParent != null)
			return this.mParent.indexOf(o);

		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		int index = -1;

		if (this.mParent != null)
			index = this.mParent.lastIndexOf(o);

		if (index >= 0)
			return index;

		return this.localItems.lastIndexOf(o);
	}


	public int parentSize() {
		if (this.mParent != null)
			return this.mParent.size();

		return 0;
	}

	public int localSize() {
		return this.localItems.size();
	}

	@Override
	public Iterator<T> iterator() {

		return new Iterator<T>() {
			int index = -1;

			@Override
			public T next() {
				if (!this.hasNext())
					throw new NoSuchElementException();

				this.index += 1;
				return BranchingList.this.get(this.index);
			}

			@Override
			public boolean hasNext() {
				return index < BranchingList.this.size();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return this.localItems.subList(fromIndex, toIndex);
	}

	@Override
	public ListIterator<T> listIterator() {
		throw new RuntimeException("Not supported");
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public void add(int index, T element) {
		this.localItems.add(index, element);
	}
}
