import { ResthubPage } from './app.po';

describe('resthub App', function() {
  let page: ResthubPage;

  beforeEach(() => {
    page = new ResthubPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
