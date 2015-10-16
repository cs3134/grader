import java.util.List;
import java.util.LinkedList;

public class BST {

  int data;
  BST left, right;

  public BST(int data) {
    this(data, null, null);
  }

  public BST(int data, BST left, BST right) {
    this.data = data;
    this.left = left;
    this.right = right;
  }

  public void insert(int data) {
    if (data <= this.data) {
      if (this.left == null) {
        this.left = new BST(data);
      } else {
        this.left.insert(data);
      }
    } else {
      if (this.right == null) {
        this.right = new BST(data);
      } else {
        this.right.insert(data);
      }
    }
  }

  public BST find(int data) {
    if (data == this.data) {
      return this;
    } else if (data <= this.data) {
      if (this.left == null) {
        return null;
      } else {
        return this.left.find(data);
      }
    } else {
      if (this.right == null) {
        return null;
      } else {
        return this.right.find(data);
      }
    }
  }

  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("(");
    s.append(this.data);
    if (this.left != null) {
      s.append(" ");
      s.append(this.left.toString());
    }
    if (this.right != null) {
      s.append(" ");
      s.append(this.right.toString());
    }
    s.append(")");
    return s.toString();
  }

  public static boolean isBst(BST n) {
    if (n == null) {
      return true;
    }
    if (n.left != null && n.left.data > n.data ||
        n.right != null && n.right.data < n.data) {
      return false;
    }
    return isBst(n.left) && isBst(n.right);
  }

  public static List<Integer> kMin(BST n, int k) {
    if (k == 0 || n == null) {
        return new LinkedList<Integer>();
    }
    List<Integer> res = kMin(n.left, k);
    if (res.size() == k) {
      return res;
    }
    res.add(n.data);
    if (res.size() == k) {
      return res;
    }
    for (Integer data : kMin(n.right, k - res.size())) {
      res.add(data);
    }
    return res;
  }

  public static List<Integer> getInterval(BST n, int i, int j) {
    if (n == null) {
      return new LinkedList<Integer>();
    }
    if (n.data < i) {
      return getInterval(n.right, i, j);
    }
    if (n.data > j) {
      return getInterval(n.left, i, j);
    }
    List<Integer> res = getInterval(n.left, i, j);
    res.add(n.data);
    for (Integer data : getInterval(n.right, i, j)) {
      res.add(data);
    }
    return res;
  }

  public static void main(String[] args) {
    BST n = new BST(5);
    int[] vals = {3, 7, 2, 4, 6, 8};
    for (Integer val : vals) {
      n.insert(val);
    }

    System.out.println(n);

    // a
    BST nonBST = new BST(
      5,
      new BST(
        3,
        new BST(2),
        new BST(4)
      ),
      new BST(
        8,
        new BST(6),
        new BST(7)
      )
    );

    System.out.println(isBst(n));
    System.out.println(isBst(nonBST));

    // b
    for (int i = 0; i < 8; i++) {
      System.out.println(kMin(n, i));
    }

    // c
    System.out.println(getInterval(n, 4, 6));
    System.out.println(getInterval(n, 3, 7));
    System.out.println(getInterval(n, 10, 12));
  }
}
