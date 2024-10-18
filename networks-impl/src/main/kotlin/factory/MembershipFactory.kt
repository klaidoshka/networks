package factory

import model.Membership

object MembershipFactory : Factory<Membership> {

    override fun create(): Membership {
        return Membership()
    }
}