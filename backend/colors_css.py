# Create CSS using GitHub's colour scheme from a JSON source like (https://github.com/doda/github-language-colors)

import json

with open('github_colors.json') as colors:
   with open('github_colors.css', 'w') as css:
      m = json.loads(colors.read())
      for lang in m:
          color = m[lang]
          lang_safe = lang.replace('+', 'plus').replace('#','sharp').replace(' ','')
          css.write('.project-{0} {{ border-bottom: 5px solid {1}; }}\n'.format(lang_safe, m[lang]))
