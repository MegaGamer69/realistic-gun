import java.util.ArrayList;

public class Main
{
	private static void reload(Magazine magazine, int max)
	{
		for(int i = 0; i < max; i++)
		{
			System.out.println("Colocando bala de número " + (i + 1) + ".");
			
			magazine.putBullet(new Bullet("5.56x45mm NATO"));
		}
	}
	
	private static void fire(Weapon weapon, int max)
	{
		for(int i = 0; i < max; i++)
		{
			weapon.fire();
		}
	}
	
	public static void main(String[] args)
	{
		testColtM4A1();
	}
	
	private static void testColtM4A1()
	{
		Bolt bolt = new Bolt();
		Chamber chamber = new Chamber();
		Ejector ejector = new Ejector();
		Hammer hammer = new Hammer();
		Magazine magazine = new Magazine(30);
		Trigger trigger = new Trigger();
		
		bolt.setEjector(ejector);
		bolt.setHammer(hammer);
		chamber.setEjector(ejector);
		hammer.setEjector(ejector);
		trigger.setHammer(hammer);
		
		Weapon m4a1 = new Weapon(bolt, chamber, ejector, hammer, magazine, trigger);
		
		fire(m4a1, 1);
		reload(magazine, 20);
		fire(m4a1, 21);
	}
}

class Bullet
{
	private String caliber = "Nameless Caliber";
	
	private boolean ejected = false;
	private boolean fired = false;
	
	public Bullet(String calName)
	{
		caliber = (calName != null) ? calName : caliber;
	}
	
	public String getCaliber()
	{
		return(caliber);
	}
	
	public boolean tryToFire()
	{
		return(!fired && !ejected);
	}
	
	public void eject()
	{
		ejected = true;
		
		if(fired)
		{
			System.out.println("A arma disparou a bala. BOOM!");
		}
		else
		{
			System.out.println("A arma ejetou a bala sem sucesso.");
		}
	}
	
	public void fire()
	{
		if(tryToFire())
		{
			fired = true;
			
			System.out.println(String.format("A bala %s foi disparada.", caliber));
		}
	}
	
	public boolean getState()
	{
		return(fired);
	}
	
	public boolean isEjected()
	{
		return(ejected);
	}
}

class WeaponPiece
{
	protected float dirtiness = 0F; // Nível de sujeira.
	protected float careness = 1F; // Nível de cuidado contra desgaste.
	private boolean activated = false;
	
	protected WeaponPiece[] dependencies = null;
	
	public WeaponPiece(WeaponPiece[] deps)
	{
		dependencies = (deps != null) ? deps : new WeaponPiece[0];
	}
	
	public void printStatus()
	{
		System.out.println(String.format("%s(dirtiness=%.4f;careness=%.4f;)\n", toString(), dirtiness, careness));
	}
	
	public final boolean isWorking()
	{
		return(dirtiness <= 0.3F && careness >= 0.7F);
	}
	
	public final boolean canWork()
	{
		if(!isWorking())
		{
			return(false);
		}
		
		for(WeaponPiece piece : dependencies)
		{
			if(piece != null && !piece.canWork())
			{
				return(false);
			}
		}
		
		return(true);
	}
	
	// Ative o funcionamento da peça (ex.: ejete a capsula pelo ejetor).
	
	public void activate()
	{
		activated = true;
		
		dirtiness += 0.002F;
		careness -= 0.0005F;
	}

	public void reset()
	{
		activated = false;
	}
}

class Magazine extends WeaponPiece
{
	private int capacity = 0;
	
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	public Magazine(int cap)
	{
		super(null);
		
		capacity = Math.max(cap, 0);
		bullets = new ArrayList<Bullet>();
	}
	
	public void putBullet(Bullet bullet)
	{
		if(bullets.size() < capacity && bullet != null)
		{
			bullets.add(bullet); // Por realismo, coloque uma bala no pente manualmente.
			System.out.printf("Adicionado uma (1) bala ao pente. (%s)\n", bullet.getCaliber());
		}
		else
		{
			System.out.println("Você excedeu o limite de balas.");
		}
	}
	
	public Bullet feedBullet()
	{
		if(canWork() && bullets.size() > 0)
		{
			super.activate();
			
			Bullet b = bullets.remove(0);
			
			System.out.println("O projétil foi alimentado.");
			
			return(b);
		}
		else
		{
			System.out.println("Houve uma falha ao alimentar o projétil.");
			
			return(null);
		}
	}
	
	public int getBulletAmount()
	{
		return(bullets.size());
	}
	
	@Override
	public void activate()
	{
		if(isWorking())
		{
			super.activate();
			
			feedBullet();
		}
		else
		{
			System.out.println(String.format("Houve uma falha ao ativar o %s.", toString()));
		}
	}

	@Override
	public String toString()
	{
		return("Pente");
	}
}

class Ejector extends WeaponPiece
{
	private Bullet cartridge = null;
	
	public Ejector()
	{
		super(new WeaponPiece[1]);
	}
	
	public void put(Bullet instance)
	{
		cartridge = instance;
	}
	
	@Override
	public void activate()
	{
		if(canWork() && cartridge != null)
		{
			super.activate();
			
			cartridge.eject();
			System.out.println("A capsula disparada foi ejetada com sucesso.");
			
			cartridge = null;
		}
		else
		{
			System.out.println(String.format("Houve uma falha ao ativar o %s.", toString()));
		}
	}
	
	public void setCartridge(Bullet value)
	{
		cartridge = value;
	}

	@Override
	public String toString()
	{
		return("Ejetor");
	}
}

class Trigger extends WeaponPiece
{
	private Hammer hammer = null;
	
	public Trigger()
	{
		super(new WeaponPiece[1]);
	}
	
	@Override
	public void activate()
	{
		if(canWork() && hammer != null)
		{
			super.activate();
			
			System.out.println("O gatilho foi pressionado");
			hammer.activate();
		}
		else
		{
			System.out.println(String.format("Houve uma falha ao ativar o %s.", toString()));
		}
	}
	
	public void setHammer(Hammer value)
	{
		hammer = value;
		
		dependencies[0] = hammer;
	}

	@Override
	public String toString()
	{
		return("Gatilho");
	}
}

class Hammer extends WeaponPiece
{
	private Ejector ejector = null;
	
	public Hammer()
	{
		super(new WeaponPiece[1]);
	}
	
	@Override
	public void activate()
	{
		if(canWork())
		{
			super.activate();
			
			System.out.println("O martelo foi acionado.");
		}
	}
	
	public void setEjector(Ejector value)
	{
		ejector = value;
		
		dependencies[0] = ejector;
	}

	public Ejector getEjector()
	{
		return(ejector);
	}

	@Override
	public String toString()
	{
		return("Martelo");
	}
}

class Bolt extends WeaponPiece
{
	private Ejector ejector = null;
	private Hammer hammer = null;
	
	public Bolt()
	{
		super(new WeaponPiece[2]);
	}
	
	@Override
	public void activate()
	{
		if(canWork() && ejector != null && hammer != null)
		{
			super.activate();
			
			System.out.println("O ferrolho foi ativado.");
			ejector.activate();
		}
	}

	public void setEjector(Ejector value)
	{
		ejector = value;
		
		dependencies[0] = value;
	}

	public void setHammer(Hammer value)
	{
		hammer = value;
		
		dependencies[1] = value;
	}
	
	public Ejector getEjector()
	{
		return(ejector);
	}

	public Hammer getHammer()
	{
		return(hammer);
	}

	@Override
	public String toString()
	{
		return("Ferrolho");
	}
}

class Chamber extends WeaponPiece
{
	private Bullet loadedBullet = null;
	private Ejector ejector = null;
	
	public Chamber()
	{
		super(new WeaponPiece[1]);
	}
	
	public void setEjector(Ejector value)
	{
		ejector = value;
		
		dependencies[0] = value;
	}
	
	public Ejector getEjector()
	{
		return(ejector);
	}
	
	public void load(Bullet bullet)
	{
		if(!canWork()) return;
		
		if(loadedBullet != null) eject();
		
		System.out.println("A câmara foi carregada.");
		
		loadedBullet = bullet;
		
		activate();
	}
	
	public void eject()
	{
		if(loadedBullet != null)
		{
			ejector.put(loadedBullet);
			loadedBullet.eject();
			
			loadedBullet = null;
			
			activate();
		}
	}
	
	public void fire()
	{
		if(canWork() && loadedBullet != null)
		{
			System.out.println("A câmara disparou a bala.");
			
			loadedBullet.fire();
			ejector.put(loadedBullet);
			
			loadedBullet = null;
			
			activate();
		}
	}
	
	@Override
	public void activate()
	{
		super.activate();
	}
	
	@Override
	public String toString()
	{
		return("Câmara");
	}
}

class Weapon
{
	private WeaponPiece[] pieces = null;
	
	public Weapon(WeaponPiece... pieces)
	{
		this.pieces = new WeaponPiece[6];
		
		for(int i = 0; i < this.pieces.length; i++)
		{
			this.pieces[i] = pieces[i];
		}
	}
	
	public void fire()
	{
		for(WeaponPiece piece : pieces)
		{
			piece.reset();
		}

		Magazine magazine = null;
		Hammer hammer = null;
		Trigger trigger = null;
		Bolt bolt = null;
		Chamber chamber = null;
		
		for(WeaponPiece piece : pieces)
		{
			if(piece instanceof Magazine) magazine = (Magazine)(piece);
			if(piece instanceof Hammer) hammer = (Hammer)(piece);
			if(piece instanceof Trigger) trigger = (Trigger)(piece);
			if(piece instanceof Bolt) bolt = (Bolt)(piece);
			if(piece instanceof Chamber) chamber = (Chamber)(piece);
		}
		
		if(magazine != null && hammer != null && trigger != null && bolt != null && chamber != null)
		{
			Bullet bullet = magazine.feedBullet();
			
			if(bullet != null)
			{
				chamber.load(bullet);
				trigger.activate();
				chamber.fire();
				bolt.activate();
			}
			else
			{
				System.out.println("Está sem munição suficiente.");
			}
		}
		else
		{
			System.out.println("A arma está incompleta.");
		}
	}
}
