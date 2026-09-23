# {{jreleaserCreationStamp}}
{{! Flatmark does not self-update, and its postinstall symlink must be removed together with the package. }}
{{#brewRequireRelative}}
require_relative "{{.}}"
{{/brewRequireRelative}}

cask "{{brewCaskName}}" do
  desc "Static site generator"
  homepage "{{projectLinkHomepage}}"
  url "{{distributionUrl}}"{{#brewDownloadStrategy}}, :using => {{.}}{{/brewDownloadStrategy}},
      verified: "{{repoHost}}"
  version "{{projectVersion}}"
  sha256 "{{distributionChecksumSha256}}"
  name "{{brewCaskDisplayName}}"
  {{#brewCaskHasAppcast}}
  appcast {{brewCaskAppcast}}
  {{/brewCaskHasAppcast}}

  {{#brewHasLivecheck}}
  livecheck do
    {{#brewLivecheck}}
    {{.}}
    {{/brewLivecheck}}
  end
  {{/brewHasLivecheck}}
  {{#brewDependencies}}
  depends_on {{.}}
  {{/brewDependencies}}

  {{#brewCaskHasPkg}}
  pkg "{{brewCaskPkg}}"
  {{/brewCaskHasPkg}}
  {{#brewCaskHasApp}}
  app "{{brewCaskApp}}"
  {{/brewCaskHasApp}}
  {{#brewCaskHasBinary}}
  binary "{{distributionArtifactRootEntryName}}/bin/{{distributionExecutableUnix}}"
  {{/brewCaskHasBinary}}
  uninstall pkgutil: "ba.sake.flatmark",
            delete: "/usr/local/bin/flatmark"
  {{#brewCaskHasZap}}
  {{#brewCaskZap}}
  zap {{name}}: [
    {{#items}}
    "{{.}}",
    {{/items}}
  ]
  {{/brewCaskZap}}
  {{/brewCaskHasZap}}
end
