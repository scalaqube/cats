package cats
package data

import cats.functor.Contravariant

/**
 * [[Const]] is a phantom type, it does not contain a value of its second type parameter `B`
 * [[Const]] can be seen as a type level version of `Function.const[A, B]: A => B => A`
 */
final case class Const[A, B](getConst: A) {
  /**
   * changes the type of the second type parameter
   */
  def retag[C]: Const[A, C] =
    this.asInstanceOf[Const[A, C]]

  def combine(that: Const[A, B])(implicit A: Semigroup[A]): Const[A, B] =
    Const(A.combine(getConst, that.getConst))

  def traverse[F[_], C](f: B => F[C])(implicit F: Applicative[F]): F[Const[A, C]] =
    F.pure(retag[C])

  def ===(that: Const[A, B])(implicit A: Eq[A]): Boolean =
    A.eqv(getConst, that.getConst)

  def partialCompare(that: Const[A, B])(implicit A: PartialOrder[A]): Double =
    A.partialCompare(getConst, that.getConst)

  def compare(that: Const[A, B])(implicit A: Order[A]): Int =
    A.compare(getConst, that.getConst)

  def show(implicit A: Show[A]): String =
    s"Const(${A.show(getConst)})"
}

object Const extends ConstInstances {
  def empty[A, B](implicit A: Monoid[A]): Const[A, B] =
    Const(A.empty)
}

private[data] sealed abstract class ConstInstances extends ConstInstances0 {
  implicit def constOrder[A: Order, B]: Order[Const[A, B]] = new Order[Const[A, B]] {
    def compare(x: Const[A, B], y: Const[A, B]): Int =
      x compare y
  }

  implicit def constShow[A: Show, B]: Show[Const[A, B]] = new Show[Const[A, B]] {
    def show(f: Const[A, B]): String = f.show
  }

  implicit def constContravariant[C]: Contravariant[Const[C, ?]] = new Contravariant[Const[C, ?]] {
    override def contramap[A, B](fa: Const[C, A])(f: (B) => A): Const[C, B] =
      fa.retag[B]
  }

  implicit def constTraverse[C]: Traverse[Const[C, ?]] = new Traverse[Const[C, ?]] {
    def traverse[G[_]: Applicative, A, B](fa: Const[C, A])(f: A => G[B]): G[Const[C, B]] =
      fa.traverse(f)

    def foldLeft[A, B](fa: Const[C, A], b: B)(f: (B, A) => B): B = b

    def foldRight[A, B](fa: Const[C, A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = lb
  }

  implicit def constMonoid[A: Monoid, B]: Monoid[Const[A, B]] = new Monoid[Const[A, B]]{
    def empty: Const[A, B] =
      Const.empty

    def combine(x: Const[A, B], y: Const[A, B]): Const[A, B] =
      x combine y
  }
}

private[data] sealed abstract class ConstInstances0 extends ConstInstances1 {
  implicit def constPartialOrder[A: PartialOrder, B]: PartialOrder[Const[A, B]] = new PartialOrder[Const[A, B]]{
    def partialCompare(x: Const[A, B], y: Const[A, B]): Double =
      x partialCompare y
  }

  implicit def constApplicative[C: Monoid]: Applicative[Const[C, ?]] = new Applicative[Const[C, ?]] {
    def pure[A](x: A): Const[C, A] =
      Const.empty

    def ap[A, B](fa: Const[C, A])(f: Const[C, A => B]): Const[C, B] =
      f.retag[B] combine fa.retag[B]
  }
}

private[data] sealed abstract class ConstInstances1 {
  implicit def constEq[A: Eq, B]: Eq[Const[A, B]] = new Eq[Const[A, B]] {
    def eqv(x: Const[A, B], y: Const[A, B]): Boolean =
      x === y
  }

  implicit def constApply[C: Semigroup]: Apply[Const[C, ?]] = new Apply[Const[C, ?]] {
    def ap[A, B](fa: Const[C, A])(f: Const[C, A => B]): Const[C, B] =
      fa.retag[B] combine f.retag[B]

    def map[A, B](fa: Const[C, A])(f: A => B): Const[C, B] =
      fa.retag[B]
  }
}
