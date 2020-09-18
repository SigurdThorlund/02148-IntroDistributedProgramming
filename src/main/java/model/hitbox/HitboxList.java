package model.hitbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// A list of hitbox, meant to be used for storing a GameObjects hitboxes
// (and not all hitboxes in the game).
public class HitboxList {

    private List<Hitbox> hitboxList = new ArrayList<Hitbox>();

    public HitboxList(Hitbox hitbox) {
        hitboxList.add(hitbox);
    }

    public HitboxList(Collection<Hitbox> hbs) {
        hitboxList.addAll(hbs);
    }

    public void addHitbox(Hitbox hb) {
        hitboxList.add(hb);
    }

    public void addHitboxes(Collection<Hitbox> hbs) {
        hitboxList.addAll(hbs);
    }

    public Hitbox getHitbox(int i) {
        return hitboxList.get(i);
    }

    public boolean collision(Hitbox hb) {
        for (Hitbox hitbox : hitboxList) {
            if (hitbox.collision(hb)) {
                return true;
            }
        }
        return false;
    }

    public boolean collision(HitboxList hbl) {
        for (Hitbox hitboxA : hitboxList) {
            for (Hitbox hitboxB : hbl.getHitboxList()) {
                if (hitboxA.collision(hitboxB)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Hitbox> getHitboxList() {
        return hitboxList;
    }
}
