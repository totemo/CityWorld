package me.daddychurchill.CityWorld.Context.Astral;

import me.daddychurchill.CityWorld.WorldGenerator;
import me.daddychurchill.CityWorld.Context.NatureContext;
import me.daddychurchill.CityWorld.Plats.PlatLot;
import me.daddychurchill.CityWorld.Plats.Astral.AstralBuildingLot;
import me.daddychurchill.CityWorld.Plats.Astral.AstralEmptyLot;
import me.daddychurchill.CityWorld.Plats.Astral.AstralNatureLot;
import me.daddychurchill.CityWorld.Plats.Astral.AstralShipLot;
import me.daddychurchill.CityWorld.Plats.Nature.BunkerLot.BunkerType;
import me.daddychurchill.CityWorld.Plugins.ShapeProvider;
import me.daddychurchill.CityWorld.Support.HeightInfo;
import me.daddychurchill.CityWorld.Support.Odds;
import me.daddychurchill.CityWorld.Support.PlatMap;
import me.daddychurchill.CityWorld.Support.SupportChunk;

public class AstralNatureContext extends NatureContext {
	
	public AstralNatureContext(WorldGenerator generator) {
		super(generator);
		
		oddsOfIsolatedLots = Odds.oddsPrettyUnlikely;
		oddsOfUnfinishedBuildings = Odds.oddsUnlikely;
		oddsOfIsolatedConstructs = Odds.oddsPrettyUnlikely;
	}
	
	@Override
	public PlatLot createNaturalLot(WorldGenerator generator, PlatMap platmap, int x, int z) {
		return new AstralNatureLot(platmap, platmap.originX + x, platmap.originZ + z);
	}
	
	@Override
	public void populateMap(WorldGenerator generator, PlatMap platmap) {
		
		//TODO, Nature doesn't handle schematics quite right yet
		// let the user add their stuff first, then plug any remaining holes with our stuff
		//mapsSchematics.populate(generator, platmap);
		
		// random fluff
		Odds odds = platmap.getOddsGenerator();
		ShapeProvider shapeProvider = generator.shapeProvider;
		
		// where it all begins
		int originX = platmap.originX;
		int originZ = platmap.originZ;
		HeightInfo heights;
		boolean addingBases = false;
		
		// is this natural or buildable?
		for (int x = 0; x < PlatMap.Width; x++) {
			for (int z = 0; z < PlatMap.Width; z++) {
				PlatLot current = platmap.getLot(x, z);
				if (current == null) {
					
					// what is the world location of the lot?
					int blockX = (originX + x) * SupportChunk.chunksBlockWidth;
					int blockZ = (originZ + z) * SupportChunk.chunksBlockWidth;
					
					// get the height info for this chunk
					heights = HeightInfo.getHeightsFaster(generator, blockX, blockZ);
					if (!heights.anyEmpties && heights.averageHeight < generator.seaLevel - 8) {
						if (!addingBases)
							addingBases = odds.playOdds(oddsOfIsolatedLots);
						
						if (addingBases) {
							if (odds.playOdds(oddsOfUnfinishedBuildings)) 
								current = new AstralEmptyLot(platmap, originX + x, originZ + z);
							
							else {
								switch (odds.getRandomInt(7)) {
								case 1:
									current = new AstralBuildingLot(platmap, originX + x, originZ + z, BunkerType.BALLSY);
									break;
								case 2:
									current = new AstralBuildingLot(platmap, originX + x, originZ + z, BunkerType.FLOORED);
									break;
								case 3:
									current = new AstralBuildingLot(platmap, originX + x, originZ + z, BunkerType.GROWING);
									break;
								case 4:
									current = new AstralBuildingLot(platmap, originX + x, originZ + z, BunkerType.PYRAMID);
									break;
								case 5:
									current = new AstralBuildingLot(platmap, originX + x, originZ + z, BunkerType.QUAD);
									break;
								case 6:
									current = new AstralBuildingLot(platmap, originX + x, originZ + z, BunkerType.RECALL);
									break;
								default:
									current = new AstralBuildingLot(platmap, originX + x, originZ + z, BunkerType.TANK);
									break;
								}
							}
						}
						
					} else if (shapeProvider.isIsolatedConstructAt(originX + x, originZ + z, oddsOfIsolatedConstructs))
						current = new AstralShipLot(platmap, originX + x, originZ + z);
				}
					
				// did current get defined?
				if (current != null)
					platmap.setLot(x, z, current);
				else
					platmap.recycleLot(x, z);
			}
		}
	}
}