package DataStructure;

public class LinkedList {
    private Node front , back;
    private int size;

    public int getSize() {
        return size;
    }

    public LinkedList(){
        front=back=null;
        size=0;
    }

    public void addFirst(Object element){
        Node newNode = new Node(element);
        if (size==0)
            front=back=newNode;
        else {
            newNode.setNext(front);
            front=newNode;
        }
        size++;
    }

    public Object getFirst(){
        return front.getElement();
    }

    public void addLast(Object element){
        Node newNode = new Node(element);
        if (size==0)
            front=back=newNode;
        else {
            back.setNext(newNode);
            back=newNode;
        }
        size++;
    }

    public Object getLast(){
        return back.getElement();
    }

    public void add(int index , Object element){
        if (index==0)addFirst(element);
        else if (index==size-1)addLast(element);
        else {
            Node newNode = new Node(element);
            Node current = front;
            for (int i = 0 ; i < index-1 ; ++i){
                current=current.getNext();
            }
            newNode.setNext(current.getNext());
            current.setNext(newNode);
            size++;
        }
    }

    public Object get(int index){
        if (size==0)return null;
        else if (index==0)return getFirst();
        else if (index==size-1)return getLast();
        else if (index > 0 && index < size-1) {
            Node current = front;
            for (int i = 0 ; i < index ; ++i)
                current=current.getNext();
            return current.getElement();
        }
        return null;
    }

    public boolean removeFirst(){
        if (size==0)
            return false;
        else if (size==1)
            front=back=null;
        else
            front=front.getNext();
        size--;
        return true;
    }

    public boolean removeLast(){
        if (size==0)
            return false;
        else if (size==1)
            front=back=null;
        else{
            Node current = front;
            for (int i = 0 ; i < size-2 ; ++i)
                current=current.getNext();
            current.setNext(null);
            back=current;
        }
        size--;
        return true;
    }

    public boolean remove(int index) {
        if (size == 0) return false;
        else if (index == 0) return removeFirst();
        else if (index == size - 1) return removeLast();
        else if (index > 0 && index < size - 1) {
            Node current = front;
            for (int i = 0; i < index-1; ++i)
                current = current.getNext();

            current.setNext(current.getNext().getNext());
            size--;
            return true;
        }
        else return false;
    }

    public void printList(Node current){
        if (current!=null){
            System.out.println(current.getElement());
            printList(current.getNext());
        }
    }

    public void printList() {
        printList(front);
    }

    public void clear(){
        front=back=null;
        size=0;
    }

    public int find(Object element){
        Node current = front;
        for (int i = 0 ; i < size ; ++i){
            if (current.getElement().equals(element))
                return i;
            current=current.getNext();
        }
        return -1;
    }


    public Node getFront() {
        return front;
    }
}
