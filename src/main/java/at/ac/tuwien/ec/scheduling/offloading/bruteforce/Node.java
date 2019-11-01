package at.ac.tuwien.ec.scheduling.offloading.bruteforce;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;

/**
 * Represents a node of the Tree<T> class. The Node<T> is also a container, and
 * can be thought of as instrumentation to determine the location of the type T
 * in the Tree<T>.
 */
public class Node<T> {
 
    public T data;
    public Node<T> leftMostChildren, rightSibling;
 
    /**
     * Default ctor.
     */
    public Node() {
        super();
        leftMostChildren = null;
        rightSibling = null;
    }
 
    /**
     * Convenience ctor to create a Node<T> with an instance of T.
     * @param data an instance of T.
     */
    public Node(T data) {
        this();
        setData(data);
    }
     
    /**
     * Return the children of Node<T>. The Tree<T> is represented by a single
     * root Node<T> whose children are represented by a List<Node<T>>. Each of
     * these Node<T> elements in the List can have children. The getChildren()
     * method will return the children of a Node<T>.
     * @return the children of Node<T>
     */
    public Node<T> getChildren() {
        return this.leftMostChildren;
    }
 
    /**
     * Sets the children of a Node<T> object. See docs for getChildren() for
     * more information.
     * @param children the List<Node<T>> to set.
     */
    public void setChildren(Node<T> children) {
        this.leftMostChildren = children;
    }
 
    /**
     * Returns the number of immediate children of this Node<T>.
     * @return the number of immediate children.
     */
    //public int getNumberOfChildren() {
      //  if (children == null) {
        //    return 0;
        //}
        //return children.size();
    //}
     
    /**
     * Adds a child to the list of children for this Node<T>. The addition of
     * the first child will create a new List<Node<T>>.
     * @param child a Node<T> object to set.
     */
    public void addChild(Node<T> child) {
        if (leftMostChildren == null)
            leftMostChildren = child;
        else
        {
        	Node<T> currNode = this.leftMostChildren;
        	while(currNode.rightSibling!=null)
        		currNode = currNode.rightSibling;
        	currNode.rightSibling = child;
        }
        
    }
     
  
    public T getData() {
        return this.data;
    }
 
    public void setData(T data) {
        this.data = data;
    }

	public Node<T> getRightSibling() {
		// TODO Auto-generated method stub
		return rightSibling;
	}
     
   }
