package com.theoryinpractise.halbuilder.siren;

import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import org.testng.annotations.Test;

public class SirenRepresentationWriterTest {


    @Test
    public void testSirenGeneration()  {


        DefaultRepresentationFactory rf = new DefaultRepresentationFactory();
        rf.withFlag(RepresentationFactory.PRETTY_PRINT);
        rf.withRenderer("application/vnd.siren+json", SirenRepresentationWriter.class);

        Representation rep = rf.newRepresentation("/api/user/mark").withProperty("name", "Mark");
        rep.withLink("licence", "http://www.apache.org/licenses/LICENSE-2.0.html");
        Representation address = rf.newRepresentation()
                .withProperty("city", "Auckland")
                .withProperty("country", "New Zealand");
        rep.withRepresentation("address", address);

        System.out.println(rep.toString("application/vnd.siren+json"));
        System.out.println(rep.toString("application/hal+json"));




    }

}
