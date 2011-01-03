/**
 * Gets the site's pages (the child pages to the page defined as the "root" page)
 *
 * @method getSitePages
 * @return {Array} An array with the site page objects
 */
function getSitePages() {    
  var pages = new Array();
  pages = pages.concat(sitedata.findChildPages(sitedata.rootPage.id));

  // bug for openjdk which returns an array of arrays  instead of just a single array.
  if (pages.length > 0) {
     if (pages[0].getClass().isArray()) {
        var p2 = new Array(pages[0].length);
        for (var i = 0; i < pages[0].length; i++) {
           p2[i] = pages[0][i];
        }
        pages = p2;
     }
  }

  // push root page to front
  pages.unshift(sitedata.rootPage);

  return pages;
}

