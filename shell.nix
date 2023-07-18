with import <nixpkgs> { };

mkShell {

  name = "env";
  buildInputs = [
    nodejs
  ];

  shellHook = ''
  '';

}