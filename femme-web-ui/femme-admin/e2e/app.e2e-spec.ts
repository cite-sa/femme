import { FemmeAdminPage } from './app.po';

describe('femme-admin App', () => {
  let page: FemmeAdminPage;

  beforeEach(() => {
    page = new FemmeAdminPage();
  });

  it('should display welcome message', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('Welcome to app!!');
  });
});
