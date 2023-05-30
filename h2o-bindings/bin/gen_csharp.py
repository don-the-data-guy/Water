#!/usr/bin/env python
# -*- encoding: utf-8 -*-
import bindings as bi


class CSharpTypeTranslator(bi.TypeTranslator):
    def __init__(self):
        bi.TypeTranslator.__init__(self)
        self.types["boolean"] = "bool"
        self.types["Polymorphic"] = "object"
        self.types["Object"] = "object"
        self.make_map = lambda ktype, vtype: "Dictionary<%s,%s>" % (ktype, vtype)
        self.make_key = lambda itype, schema: "string"

type_adapter = CSharpTypeTranslator()
def translate_type(h2o_type, schema):
    return type_adapter.translate(h2o_type, schema)


# ----------------------------------------------------------------------------------------------------------------------
#   Generate Schema
# ----------------------------------------------------------------------------------------------------------------------
def generate_schema(class_name, schema):
    """
    Generate C# declaration file for a schema.
    """
    has_map = False
    for field in schema["fields"]:
        if field["type"].startswith("Map"): has_map = True

    superclass = schema["superclass"]
    if superclass == "Iced": superclass = "Object"

    yield "/**"
    yield " * This file is auto-generated by h2o-3/h2o-bindings/bin/gen_csharp.py"
    yield " * Copyright 2016 H2O.ai; GNU Affero General Public License v3 (see LICENSE for details)"
    yield " */"
    yield "namespace ai.h2o"
    yield "{"
    yield "  using System;"
    yield "  using System.Collections.Generic;" if has_map else None
    yield ""
    yield "  public class {name}: {super} {{".format(name=class_name, super=superclass)

    for field in schema["fields"]:
        if field["name"] == "__meta": continue
        csharp_type = translate_type(field["type"], field["schema_name"])
        yield "    /// <summary>"
        yield bi.wrap(field["help"], "    ///   ")
        yield "    /// </summary>"
        yield "    public {type} {name} {{ get; set; }}".format(type=csharp_type, name=field["name"])
        yield ""

    yield "  }"
    yield "}"


# ----------------------------------------------------------------------------------------------------------------------
#   Generate Enum class
# ----------------------------------------------------------------------------------------------------------------------
def generate_enum(name, values):
    yield "/**"
    yield " * This file is auto-generated by h2o-3/h2o-bindings/bin/gen_csharp.py"
    yield " * Copyright 2016 H2O.ai; GNU Affero General Public License v3 (see LICENSE for details)"
    yield " */"
    yield "namespace ai.h2o"
    yield "{"
    yield "  public enum " + name + " {"
    for value in values:
        yield "    %s," % value
    yield "  }"
    yield "}"


# ----------------------------------------------------------------------------------------------------------------------
#   MAIN:
# ----------------------------------------------------------------------------------------------------------------------
def main():
    bi.init("C#", "CSharp")

    for schema in bi.schemas():
        name = schema["name"]
        bi.vprint("Generating schema: " + name)
        bi.write_to_file("h2o/%s.cs" % name, generate_schema(name, schema))

    for name, values in bi.enums().items():
        bi.vprint("Generating enum: " + name)
        bi.write_to_file("h2o/%s.cs" % name, generate_enum(name, sorted(values)))

    type_adapter.vprint_translation_map()


if __name__ == "__main__":
    main()
