# Read one ChemDraw file with ChemScanner and write a JSON record to OUT.
#
# Usage: ruby -rfileutils chemscanner_worker.rb FILE OUT
#   (-rfileutils: chem_scanner 0.1.3 uses FileUtils without requiring it)
#
# Record: {"ms": int, "status": "ok"|"error", "error": str, "structures": [[smiles, ""], ...]}
# The rdkit_chem Ruby binding has no InChI, so InChIKeys are filled in by the driver.
require "json"
require "chem_scanner"

# chem_scanner 0.1.3 (2019) adds a fresh Conformer to the molecule, writes atom coordinates
# into the molecule's own copy (get_conformer(0)), then passes the detached original to the
# stereo perception calls. RDKit 2025.09 rejects that ("no owner"). Point both calls at the
# molecule's conformer, which is the one holding the coordinates.
class RDKitChem::RWMol
  %i[detect_atom_stereo_chemistry detect_bond_stereo_chemistry].each do |name|
    original = instance_method(name)
    define_method(name) { |_conf| original.bind(self).call(get_conformer(0)) }
  end
end

path = ARGV[0]
start = Process.clock_gettime(Process::CLOCK_MONOTONIC)
rec = { status: "ok", error: "", structures: [] }
begin
  doc = path.downcase.end_with?(".cdxml") ? ChemScanner::Cdxml.new : ChemScanner::Cdx.new
  raise "not a readable ChemDraw file" if doc.read(path) == false

  doc.molecules.each do |m|
    smi = m.get_cano_smiles.to_s
    rec[:structures] << [smi, ""] unless smi.empty?
  end
rescue StandardError, RDKitChem::GenericRDKitException => e
  rec[:status] = "error"
  rec[:error] = "#{e.class}: #{e.message}".lines.first.to_s.strip[0, 200]
end
rec[:ms] = ((Process.clock_gettime(Process::CLOCK_MONOTONIC) - start) * 1000).round
File.write(ARGV[1], JSON.generate(rec))
