package com.theoryinpractise.halbuilder.siren;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.RepresentationException;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.api.RepresentationWriter;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.theoryinpractise.halbuilder.impl.api.Support.HREF;
import static com.theoryinpractise.halbuilder.impl.api.Support.HREFLANG;
import static com.theoryinpractise.halbuilder.impl.api.Support.NAME;
import static com.theoryinpractise.halbuilder.impl.api.Support.PROFILE;
import static com.theoryinpractise.halbuilder.impl.api.Support.TEMPLATED;
import static com.theoryinpractise.halbuilder.impl.api.Support.TITLE;


public class SirenRepresentationWriter implements RepresentationWriter<String> {

    public void write(ReadableRepresentation representation, Set<URI> flags, Writer writer) {

        JsonFactory f = new JsonFactory();
        f.setCodec(new ObjectMapper());
        f.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        try {
            JsonGenerator g = f.createJsonGenerator(writer);
            if (flags.contains(RepresentationFactory.PRETTY_PRINT)) {
                g.setPrettyPrinter(new DefaultPrettyPrinter());
            }
            g.writeStartObject();
            renderJson(g, representation, Optional.<String>absent());
            g.writeEndObject();
            g.close();
        } catch (IOException e) {
            throw new RepresentationException(e);
        }

    }

    private void renderJson(JsonGenerator g, ReadableRepresentation representation, Optional<String> embeddedRel) throws IOException {

        if (embeddedRel.isPresent()) {
            writeRelArray(g, embeddedRel.get());
        }

        if (!representation.getLinks().isEmpty()) {
            g.writeArrayFieldStart("links");

            for (Link link : representation.getLinks()) {
                g.writeStartObject();
                writeJsonLinkContent(g, link);
                g.writeEndObject();
            }

            g.writeEndArray();
        }

        if (!representation.getProperties().isEmpty()) {
            g.writeObjectFieldStart("properties");

            for (Map.Entry<String, Object> entry : representation.getProperties().entrySet()) {
                if (entry.getValue() != null) {
                    g.writeObjectField(entry.getKey(), entry.getValue());
                } else {
                    g.writeNullField(entry.getKey());
                }
            }

            g.writeEndObject();
        }

        if (!representation.getResources().isEmpty()) {
            g.writeArrayFieldStart("entities");

            Map<String, Collection<ReadableRepresentation>> resourceMap = representation.getResourceMap();

            for (Map.Entry<String, Collection<ReadableRepresentation>> resourceEntry : resourceMap.entrySet()) {
                String rel = resourceEntry.getKey();
                for (ReadableRepresentation subRepresentation : resourceEntry.getValue()) {
                    g.writeStartObject();
                    renderJson(g, subRepresentation, Optional.of(rel));
                    g.writeEndObject();
                }
            }
            g.writeEndArray();
        }
    }

    private void writeJsonLinkContent(JsonGenerator g, Link link) throws IOException {

        writeRelArray(g, link.getRel());

        g.writeStringField(HREF, link.getHref());
        if (!Strings.isNullOrEmpty(link.getName())) {
            g.writeStringField(NAME, link.getName());
        }
        if (!Strings.isNullOrEmpty(link.getTitle())) {
            g.writeStringField(TITLE, link.getTitle());
        }
        if (!Strings.isNullOrEmpty(link.getHreflang())) {
            g.writeStringField(HREFLANG, link.getHreflang());
        }
        if (!Strings.isNullOrEmpty(link.getProfile())) {
            g.writeStringField(PROFILE, link.getProfile());
        }
        if (link.hasTemplate()) {
            g.writeBooleanField(TEMPLATED, true);
        }
    }

    private void writeRelArray(JsonGenerator g, final String relString) throws IOException {
        Iterable<String> rels = Splitter.on(" ").split(relString);
        g.writeArrayFieldStart("rel");
        for (String rel : rels) {
            g.writeString(rel);
        }
        g.writeEndArray();
    }
}
